{//BEGIN_DECLARE
	name:user.LoginDialog,
	title:用户登录
}//END_DECLARE

define(["sodium","sys/BasePage"],function(sodium,BasePage){
	return sodium.declare(BasePage,{
		onCreateConfig:null,
		onCreatePage:function(){
			return this.createPageForm("user.Login",{minrecords:1,maxrecords:1});
		},
		onOpenPage:function(data){
			
		},
		doUserLoginActionSuccess:function(data){
			this.closePage();
		}
	});
});