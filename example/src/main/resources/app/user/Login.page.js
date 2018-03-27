{//BEGIN_DECLARE
	name:user.Login,
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
//		onSaveAction:function(cfg){
//			var rs=this.getRecordSet(cfg.form).getRecords()[0];
//			rs.getField("dept").setValue(["2ecea54adf6148ada03055d6a3e8daf9","董事会"]);
//		},
		onCallUserLoginComplete:function(result){
			if(result.response.head.faultcode=="ok"){
				this.openPageWindow("user.Home",{target:"self","w-menu":"main"});
			};
		}
	});
});