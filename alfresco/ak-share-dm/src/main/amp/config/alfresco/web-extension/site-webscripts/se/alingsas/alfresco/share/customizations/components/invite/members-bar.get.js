// overridden /projects/slingshot/config/alfresco/site-webscripts/org/alfresco/components/invite/members-bar.get.js

function akMain()
{

   // Add Membership Requests link (if user is manager)
   if (model.isManager)
   {
      model.links.push(
      {
         id: "membership-requests-link",
         href: "membership-requests",
         cssClass: (model.activePage == "membership-requests") ? "theme-color-4" : null,
         label: msg.get("link.membership-requests")
      });
   }
}
akMain();