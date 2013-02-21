
function main()
{
   // Check for IMAP server status
   var result = remote.call("/imap/servstatus"),
      imapServerEnabled = (result.status == 200 && result == "enabled");

   // Prepare the model for the template
   model.imapServerEnabled = imapServerEnabled;
   
   result = remote.call("/alingsas/user/createsiteenabled");
   // Check if user is allowed to create a site
   model.createSiteEnabled = (result.status == 200 && result == "true");
   
   logger.log("SitesResult: "+result+", Status:"+result.status);
   
   
}

main();