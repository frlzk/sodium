{//BEGIN_DECLARE
	name:sys.Componentdojo1_8
}//END_DECLARE

define(["sodium","dijit/layout/BorderContainer"],function(sodium,BorderContainer){
	return {
		createBorderLayout:function(param){
			var mainPanel=new BorderContainer({
				design: "headline",
				gutters:false
			});
			var its=param.items;
			for(var k=0;k<its.length;k++){
				var i=its[k];
				i.item["region"]=i.region;
				mainPanel.addChild(i.item);
			}
			return mainPanel;
		}
	};
});