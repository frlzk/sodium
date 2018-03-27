{//BEGIN_DECLARE
	name:sys.Utilextjs4
}//END_DECLARE

define("sys/Utilextjs4",function(BasePage){
	return {
		createBorderLayout:function(items,style){
			var panel=new Ext.panel.Panel({
					region:"center",
					layout:"border",
					frame:false,
					border:false,
					bodyBorder:false,
					items:items
				});
			return panel;
		}
	};
});