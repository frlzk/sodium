{//BEGIN_DECLARE
	name:sys.Utilextjs3
}//END_DECLARE

define("sys/Utilextjs3",function(BasePage){
	return {
		createBorderLayout:function(items,style){
			var panel=new Ext.Panel({
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