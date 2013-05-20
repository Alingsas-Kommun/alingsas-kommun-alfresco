/*
 * Copyright (C) 2012-2013 Alingsås Kommun
 *
 * This file is part of Alfresco customizations made for Alingsås Kommun
 *
 * The Alfresco customizations made for Alingsås Kommun is free software: 
 * you can redistribute it and/or modify
 * it under the terms of the GNU Lesser General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * The Alfresco customizations made for Alingsås Kommun is distributed in the 
 * hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU Lesser General Public License for more details.
 *
 * You should have received a copy of the GNU Lesser General Public License
 * along with the Alfresco customizations made for Alingsås Kommun. 
 * If not, see <http://www.gnu.org/licenses/>.
 */

package se.alingsas.alfresco.sharepoint.auth;

import java.io.IOException;

import javax.servlet.ServletContext;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;

import org.alfresco.model.ContentModel;
import org.alfresco.repo.SessionUser;
import org.alfresco.repo.management.subsystems.ActivateableBean;
import org.alfresco.repo.security.authentication.AuthenticationComponent;
import org.alfresco.repo.security.authentication.AuthenticationException;
import org.alfresco.repo.security.authentication.AuthenticationUtil;
import org.alfresco.repo.transaction.RetryingTransactionHelper;
import org.alfresco.repo.webdav.auth.AuthenticationDriver;
import org.alfresco.repo.webdav.auth.HTTPRequestAuthenticationFilter;
import org.alfresco.repo.webdav.auth.RemoteUserMapper;
import org.alfresco.repo.webdav.auth.SharepointConstants;
import org.alfresco.service.cmr.repository.NodeRef;
import org.alfresco.service.cmr.repository.NodeService;
import org.alfresco.service.cmr.security.AuthenticationService;
import org.alfresco.service.cmr.security.PersonService;
import org.alfresco.service.transaction.TransactionService;
import org.alfresco.web.bean.LoginBean;
import org.alfresco.web.bean.repository.User;
import org.apache.log4j.Logger;

/**
 * This is a modified version of {@link HTTPRequestAuthenticationFilter} for use by the sharepoint module.
 *
 * @author michaelboeckling
 */
public class ExternalAuthenticationFilter implements AuthenticationDriver, ActivateableBean, SharepointConstants {

  /**
   * The name of the ticket argument.
   */
  protected static final String ARG_TICKET = "ticket";
  private static final Logger LOG = Logger
			.getLogger(ExternalAuthenticationFilter.class);


  private AuthenticationComponent _authenticationComponent;

  private RemoteUserMapper _remoteUserMapper;

  private TransactionService _transactionService;

  private AuthenticationService _authenticationService;

  private PersonService _personService;

  private NodeService _nodeService;

  private boolean _active;

  @Override
  public boolean authenticateRequest(ServletContext context, final HttpServletRequest httpReq,
                                     HttpServletResponse httpResp) throws IOException, ServletException {
    // Get the user details object from the session
    SessionUser user = (SessionUser) httpReq.getSession().getAttribute(USER_SESSION_ATTRIBUTE);

    if (user != null) {
      // setup secure context
      _authenticationService.validate(user.getTicket());
    } else {

      final String userName = _remoteUserMapper.getRemoteUser(httpReq);
      // Throw an error if we have an unknown authentication

      if (userName != null) {
        // Get the user
        if (LOG.isDebugEnabled()) {
          LOG.debug("User = " + userName);
        }

        // Get the authorization header
        user = _transactionService.getRetryingTransactionHelper().doInTransaction(
                new RetryingTransactionHelper.RetryingTransactionCallback<SessionUser>() {

                  public SessionUser execute() throws Throwable {
                    try {
                      // Authenticate the user
                      _authenticationComponent.clearCurrentSecurityContext();
                      _authenticationComponent.setCurrentUser(userName);

                      return createUserEnvironment(httpReq.getSession(), userName, _authenticationService.getCurrentTicket(), true);
                    } catch (AuthenticationException ex) {
                      if (LOG.isDebugEnabled()) {
                        LOG.debug("Failed", ex);
                      }

                      return null;
                    }
                  }
                });

      } else {
        // Check if the request includes an authentication ticket
        String ticket = httpReq.getParameter(ARG_TICKET);

        if (ticket != null && ticket.length() > 0) {
          if (LOG.isDebugEnabled())
            LOG.debug("Logon via ticket from " + httpReq.getRemoteHost() + " ("
                    + httpReq.getRemoteAddr() + ":" + httpReq.getRemotePort() + ")" + " ticket=" + ticket);

          try {
            // Validate the ticket
            _authenticationService.validate(ticket);

            // Need to create the User instance if not already
            // available
            user = createUserEnvironment(httpReq.getSession(), _authenticationService.getCurrentUserName(), ticket, true);
          } catch (AuthenticationException authErr) {
            // Clear the user object to signal authentication failure
            if (LOG.isDebugEnabled()) {
              LOG.debug("Failed", authErr);
            }

            user = null;
          }
        }
      }

      // Check if the user is authenticated, if not then prompt again
      if (user == null) {
        // No user/ticket, force the client to prompt for logon details
        return false;
      }
    }

    return true;
  }

  @Override
  public void restartLoginChallenge(ServletContext context, HttpServletRequest request, HttpServletResponse response)
          throws IOException {
    // nothing to do here
  }

  /**
   * Callback to create the User environment as appropriate for a filter impl.
   *
   * @param session      HttpSession
   * @param userName     String
   * @param ticket       the ticket
   * @param externalAuth has the user been authenticated by SSO?
   * @return SessionUser
   * @throws IOException      Signals that an I/O exception has occurred.
   * @throws ServletException the servlet exception
   */
  private SessionUser createUserEnvironment(HttpSession session, final String userName, final String ticket,
                                            boolean externalAuth) throws IOException, ServletException {
    SessionUser user = doInSystemTransaction(new RetryingTransactionHelper.RetryingTransactionCallback<SessionUser>() {

      public SessionUser execute() throws Throwable {
        // Setup User object and Home space ID etc.
        final NodeRef personNodeRef = _personService.getPerson(userName);

        String name = (String) _nodeService.getProperty(personNodeRef, ContentModel.PROP_USERNAME);

        NodeRef homeSpaceRef = (NodeRef) _nodeService.getProperty(personNodeRef, ContentModel.PROP_HOMEFOLDER);

        return createUserObject(name, ticket, personNodeRef, homeSpaceRef);
      }

    });

    // Store the user on the session
    session.setAttribute(USER_SESSION_ATTRIBUTE, user);

    setExternalAuth(session, externalAuth);

    return user;
  }

  /**
   * Executes a callback in a transaction as the system user
   *
   * @param callback the callback
   * @return the return value from the callback
   */
  private <T> T doInSystemTransaction(final RetryingTransactionHelper.RetryingTransactionCallback<T> callback) {
    return AuthenticationUtil.runAs(new AuthenticationUtil.RunAsWork<T>() {

      public T doWork() throws Exception {
        return _transactionService.getRetryingTransactionHelper().doInTransaction(callback, _transactionService.isReadOnly());
      }

    }, AuthenticationUtil.SYSTEM_USER_NAME);
  }

  /**
   * Create the user object that will be stored in the session.
   *
   * @param userName     String
   * @param ticket       String
   * @param personNode   NodeRef
   * @param homeSpaceRef NodeRef
   * @return SessionUser
   */
  private SessionUser createUserObject(String userName, String ticket, NodeRef personNode, NodeRef homeSpaceRef) {
    String currentTicket = _authenticationService.getCurrentTicket();

    NodeRef person = _personService.getPerson(userName);

    return new User(userName, currentTicket, person);
  }

  /**
   * Sets or clears the external authentication flag on the session
   *
   * @param session      the session
   * @param externalAuth was the user authenticated externally?
   */
  private void setExternalAuth(HttpSession session, boolean externalAuth) {
    if (externalAuth) {
      session.setAttribute(LoginBean.LOGIN_EXTERNAL_AUTH, Boolean.TRUE);
    } else {
      session.removeAttribute(LoginBean.LOGIN_EXTERNAL_AUTH);
    }
  }

  /**
   * Activates or deactivates the bean
   *
   * @param active <code>true</code> if the bean is active and initialization
   *               should complete
   */
  public final void setActive(boolean active) {
    _active = active;
  }

  @Override
  public final boolean isActive() {
    return _active;
  }

  public void setAuthenticationComponent(AuthenticationComponent authenticationComponent) {
    _authenticationComponent = authenticationComponent;
  }

  public void setRemoteUserMapper(RemoteUserMapper remoteUserMapper) {
    _remoteUserMapper = remoteUserMapper;
  }

  public void setTransactionService(TransactionService transactionService) {
    _transactionService = transactionService;
  }

  public void setAuthenticationService(AuthenticationService authenticationService) {
    _authenticationService = authenticationService;
  }

  public void setPersonService(PersonService personService) {
    _personService = personService;
  }

  public void setNodeService(NodeService nodeService) {
    _nodeService = nodeService;
  }
}
