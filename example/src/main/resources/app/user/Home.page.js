{//BEGIN_DECLARE
	name:user.Home,
	title:首页,
	anchors:{label:"用户/首页",page:"main",action:"user.Home"}
}//END_DECLARE

define(["sodium","sys/BasePage"],function(sodium,BasePage){
	return sodium.declare(BasePage,{
		onCreateConfig:null,
		onCreatePage:function(){
			return null;
		},
		onOpenPage:function(data){
			
		}
	});
});