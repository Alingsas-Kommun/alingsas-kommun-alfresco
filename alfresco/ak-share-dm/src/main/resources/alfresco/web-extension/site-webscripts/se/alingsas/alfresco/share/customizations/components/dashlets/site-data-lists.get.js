//@overridden projects/slingshot/config/alfresco/site-webscripts/org/alfresco/components/dashlets/site-data-lists.get.js
/**
 * Reverse the list ordering
 */


function reverseList() {
  model.lists = model.lists.reverse();
}

reverseList();
