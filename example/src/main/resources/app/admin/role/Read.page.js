{//BEGIN_DECLARE
name:admin.role.Read,
title:用户角色,
anchors:[
         {type:"create",label:"新建",action:"admin.role.Save",page:"admin.role.List",attach:"admin.role.List"},
         {type:"view",label:"查看",action:"admin.role.Read",page:"admin.role.List",source:[admin.role.List,id],style:{}}
    ]
}//END_DECLARE

define(["sodium","admin/role/Read"+PAGE_RENDERER],function(sodium,BasePage){
	return sodium.declare(BasePage,{
		onCreateConfig:null
	});
});