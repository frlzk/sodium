{//BEGIN_DECLARE
name:admin.user.Read,
title:系统用户,
anchors:[{type:"create",label:"新建",page:"admin.user.List",action:"admin.user.Save",attach:'admin.user.UserList',style:{width:600,height:500}},
         {type:"view",label:"查看",page:"admin.user.List",action:"admin.user.Read",source:[admin.user.UserList,id],style:{width:600,height:500}}]
}//END_DECLARE

define(["sodium","sys/BasePage"],function(sodium,BasePage){
	return sodium.declare(BasePage,{
		onCreateConfig:null,
		onCreatePage:function(){
			return this.createPageForm("admin.user.UserForm",{minrecords:1,maxrecords:1});
		},
		onResetPage:function(){
			this.resetAllPageForm();
		},
		onOpenPage:function(data){
			if(data!=null&&data.id){
				var param={id:data.id};
				this.loadData("admin.user.Read",{form:"admin.user.UserForm"},param);
			}
		}
	});
});