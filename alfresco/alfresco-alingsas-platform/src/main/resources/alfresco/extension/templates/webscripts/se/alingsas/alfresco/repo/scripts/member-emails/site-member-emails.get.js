
function getMails(siteShortName) {

    var site = siteService.getSite(siteShortName);
    var members = site.listMembers(null, null);

    var emails = []
    for (userName in members) {

        var person = people.getPerson(userName);
        if(person && person.properties){
           var email = person.properties["cm:email"];
            if(email){
              emails.push(email);
           }
      }
    }

 return emails;
}

var siteName = args["siteName"];
var emails = getMails(siteName);
model.emails = emails.join(",");