{//BEGIN_DECLARE
	name:sys.Componentextjs4
}//END_DECLARE

define(["sodium","sodium/BasePage"],function(sodium,BasePage){
	var regions={
			top:"north",
			bottom:"south",
			left:"west",
			right:"est",
			center:"center"
	};
	return {
		createBorderLayout:function(param){
			var items=[],its=param.items;
			for(var k=0;k<its.length;k++){
				var i=its[k];
				i.item["region"]=regions[i.region];
				items.push(i.item);
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