{//BEGIN_DECLARE
	name:	admin.dept.List,
	title:	部门管理,
	anchors:{label:"系统管理/部门管理",page:"main",action:"admin.dept.Items"}
}//END_DECLARE


define(["sodium","sys/BasePage"],function(sodium,BasePage){
	return sodium.declare(BasePage,{
		onCreateConfig:null,
		onCreatePage:function(){
			var deptTreeCfg={
					region:"west",
					width:200,
					height:200,
					fieldMapping:{
						key:"id",text:"name"
					},
					requestParams:{
						action:"admin.dept.Items"
					},
					autoScroll:true,
					containerScroll:true,
					rootVisible:false,
					root:{expanded: true}
				};
			var deptTree=new net.sf.xmlform.extjs.TreePanel(deptTreeCfg);
			var deptForm = this.createPageForm("admin.dept.DeptForm",{minrecords:1,maxrecords:1});
			var panel=new Ext.Panel({
						border:false,
						frame:false,
						layout:"border",
						items : [deptTree,{
								layout : "fit",
								split : true,
								title :"",
								region :"center",
								items : deptForm
							}]
					});
			deptTree.on("select",this.onSelectDept,this);
			deptTree.expand();
			this.deptTree=deptTree;
			return panel;
		},
		onOpenPage:function(data){
			this.deptTree.load();
		},
		onSelectDept:function(a,node){
			this.selNode=node;
			this.loadData("admin.dept.Load",{form:"admin.dept.DeptForm"},{id:this.deptTree.getFieldValue(node,"id")});
		},
		initFormData:function(data){
			this.getRecordSet(data.form).setData(data);
		},
		onCallCustomAction:function(cfg){
			if(this.selNode==null){
				Ext.Msg.alert('错误', "请选择上级部门");
				return;
			}
			var rs=this.getRecordSet("admin.dept.DeptForm");
			rs.reset();
			var r=rs.getRecords()[0];
			r.getField("parent").setValue(this.deptTree.getFieldValue(this.selNode,"id"));
		},
		onSaveFinish:function(cbd,data){
			this.superMethod(arguments);
			if(this.selNode==null){
				this.deptTree.reloadNode(this.deptTree.getRootNode());
			}else{
				this.deptTree.reloadNode(this.selNode.parentNode);
			}
		},
		onDeleteFinish:function(cbd,data){
			var rs=this.getRecordSet("admin.dept.DeptForm");
			rs.reset();
			if(this.selNode!=null){
				this.deptTree.reloadNode(this.selNode.parentNode);
				this.selNode=null;
			}
		}
	});
});