{//BEGIN_DECLARE
	name:sys.Utildojo1_8
}//END_DECLARE

define(["sodium","dijit/layout/BorderContainer"],function(sodium,BorderContainer){
	return {
		createBorderLayout:function(items,style){
			var panel=new BorderContainer({
				});
			for(var i=0;i<items.length;i++){
				panel.addChild(items[i]);
			}
			return panel;
		}
	};
});