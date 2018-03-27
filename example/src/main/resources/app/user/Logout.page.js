{//BEGIN_DECLARE
	name:user.Logout,
	title:注销,
	anchors:{label:"用户/注销",page:"main",action:"user.Logout"}
}//END_DECLARE

define(["sodium","sys/BasePage"],function(sodium,BasePage){
	return sodium.declare(BasePage,{
		onCreateConfig:null,
		onCreatePage:function(){
			return null;
		},
		onOpenPage:function(data){
			this.loadData("user.Logout");
		},
		onLoadDataComplete:function(cbd,data){
			this.openPageWindow("user.Login",{target:"self"});
		}
	});
});