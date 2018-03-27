{//BEGIN_DECLARE
	name:admin.role.Readextjs4
}//END_DECLARE

define(["sodium","sys/BasePage"],function(sodium,BasePage){
	return {
		createBorderLayout:function(param){
			var items=[],its=param.items;
			for(var k in its){
				its[k]["region"]=k;
				items.push(its[k]);
			}
			var panel=new Ext.Panel({
				border:false,
				frame:true,
				layout:"border",
				items :items
			});
			return panel;
		}
	};
});