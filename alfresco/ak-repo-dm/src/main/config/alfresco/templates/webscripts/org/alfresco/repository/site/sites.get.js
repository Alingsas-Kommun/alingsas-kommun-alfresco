function main()
{
    // Get the filter parameters
    var nameFilter = args["nf"];
    var sitePreset = args["spf"];
    var sizeString = args["size"];
    
    
    //Alingsås customization start
    //Filter out user-homes from the result
    var resultSites = [];
    // Get the list of sites
    var allSites = siteService.getSites(nameFilter, sitePreset, sizeString != null ? parseInt(sizeString) : -1);
    
    for (var i = 0; i < allSites.length; i++)
    {
       if (allSites[i].getShortName() != "user-homes")
       {
          // Construct a new object with the site details we want...
    	   resultSites.push(allSites[i]);
       }
    }      
    model.sites = resultSites
    //Alingsås customiation end
    model.roles = (args["roles"] !== null ? args["roles"] : "managers");
}

main();