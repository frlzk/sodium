{//BEGIN_DECLARE
	name:admin.dict.List,
	title:系字典维护,
	anchors:{label:"系统管理/字典维护",page:"main",action:"admin.dict.Groups"}
}//END_DECLARE


define(["sodium","sys/BasePage","sys/Component","dijit/layout/BorderContainer"],function(sodium,BasePage,Components,BorderContainer){
	return sodium.declare(BasePage,{
		onCreateConfig:null,
		onCreatePage:function(){
			var groupForm=this.createPageForm("admin.dict.Group",{readonly:true,width:150,style:"width:150"});
			var item = this.createPageForm("admin.dict.Item",{});
			var b=new BorderContainer()
			return Components.createBorderLayout({items:[{item:groupForm,region:"left"},{item:item,region:"center"}]});
		},
		onOpenPage:function(data){
			
		}/*,
		onRecordSelect:function(evt){
			if(evt.formName=="admin.dict.Group"&&evt.select==true){
				var groupCode=evt.record.getField("code").getValue();
				this.getRecordSet("admin.dict.Item").setFieldValue("group",groupCode);
			}
		}*/
	});
});