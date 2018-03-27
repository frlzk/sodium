{//BEGIN_DECLARE
	name:admin.role.List,
	title:角色列表,
	anchors:{label:"系统管理/角色列表",page:"main",action:"admin.role.List"}
}//END_DECLARE

define(["sodium","sys/BaseQueryPage"],function(sodium,BasePage){
	return sodium.declare(BasePage,{
		onCreateConfig:null,
		getQueryParam:function(){
			var param=this.superMethod(arguments);
			param.formName="admin.role.List";
			return param;
		}
	});
});