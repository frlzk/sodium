{//BEGIN_DECLARE
	name:admin.role.Readextjs3
}//END_DECLARE

define(["sodium","sys/BasePage"],function(sodium,BasePage){
	return sodium.declare(BasePage,{
		onCreateConfig:null,
		onCreatePage:function(){
			this.roleNameFiled=new Ext.form.TextField({fieldLabel: 'name'});
			this.treeBox=new Ext.Panel({region:"center",layout:"fit"});
			this.roleForm = new Ext.Panel({
				border : false,
				frame : false,
				layout : 'border',
				autoScroll:true,
				width : 600,
				height : 400,
				tbar:this.createPageActions(this.getPageCfg().anchors),
				items : [
					new Ext.form.FormPanel({
						region:"north",
					    standardSubmit: true,
					    height:40,
					    border:false,
					    frame:false,
					    bodyPadding:10,
					    items:[this.roleNameFiled]
					}),
					this.treeBox
				]
			});
			return this.roleForm;
		},
		createActionTree:function(items){
			this.treeBox.removeAll(true);
			this.actionTree=new Ext.tree.TreePanel({
				autoScroll: true,
				rootVisible: true,
				root:{
			        text: '角色权限',
			        draggable: false,
			        id: 'source',
			        children:items,
			    }
			});
			this.actionTree.on("checkchange",this.onSelectAction,this);
			this.actionTree.on("afterrender", this.treeReady, this);
			this.treeBox.add(this.actionTree);
			this.treeBox.doLayout();
			return this.actionTree;
		},
		onSelectAction:function(curNode){
			var sel=curNode.raw.checked;
			curNode.raw.checked=sel;
			curNode.cascade(
				function(node){
					if(curNode!=node){
						node.set("checked",sel);
						node.raw.checked=sel;
					}
					return true;
				}
			);
		},
		onCallCustomAction:function(cfg){
			if(cfg.mark=="selAllAction"){
				this._doSelAll(true);
			}else if(cfg.mark=="deSelAllAction"){
				this._doSelAll(false);
			}else{
				this.superMethod(arguments);
			}
		},
		_doSelAll:function(sel){
			this.actionTree.getRootNode().cascade(
				function(node){
					node.set("checked",sel);
					node.raw.checked=sel;
					return true;
				}
			);
		},
		onCallSaveAction : function() {
			var name=this.roleNameFiled.getValue();
			if(name.length==""){
				Ext.Msg.alert("错误","请填写角色名称");
				return;
			}
			
			var actions=this.actionTree.getView().getChecked();
			if(actions.length==0){
				Ext.Msg.alert("错误","请选择功能权限!");
				return;
			}
			var param={id:this.roleId,name:name};
			var acts=[];
			for(var i=0;i<actions.length;i++){
				acts.push({name:actions[i].raw.id,leaf:actions[i].isLeaf()});
			};
			param.actions=acts;
			this.loadData("admin.role.Save",{save:true},{id:Ext.encode(param)});
		},
		treeReady:function(){
			this.actionTree.expandAll();
		},
		onLoadDataComplete:function(cbd,data){
			if(cbd&&cbd.save){
				if(data.head.faultcode!="ok"){
					this.showErrorMsg(data.head.faultstring);
					return;
				}
				this.superMethod(arguments);
				var rows=data.body.data;
				this.roleId=rows[0][2];
				this.fireEvent("saveRecord");
				this.showInfoMsg( data.head.faultstring);
			}else{
				var rows=data.body.data;
				var role=Ext.decode(rows[0][2]);
				this.roleNameFiled.setValue(role.name);
				this.createActionTree(role.actions);
			}
		},
		onResetPage:function(){
			this.roleNameFiled.setValue("");
			this.resetAllPageForm();
		},
		onOpenPage:function(data){
			this.roleId="";
			var id="";
			if(data!=null&&data.id){
				this.roleId=data.id;
				id=data.id;
			}
			this.loadData("admin.role.Read",{},{id:id});
		}
	});
});