{//BEGIN_DECLARE
	name:admin.user.List,
	title:用户管理,
	anchors:{label:"系统管理/用户管理",page:"main",action:"admin.user.List"}
}//END_DECLARE


define(["sodium","sys/BaseQueryPage"],function(sodium,BasePage){
	return sodium.declare(BasePage,{
		onCreateConfig:null,
		getQueryParam:function(){
			var param=this.superMethod(arguments);
			param.formName="admin.user.UserList";
			return param;
		}
	});
});