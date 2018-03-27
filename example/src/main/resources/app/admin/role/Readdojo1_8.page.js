{//BEGIN_DECLARE
	name:admin.role.Readdojo1_8
}//END_DECLARE

define(["sodium","sys/BasePage","dijit/form/ValidationTextBox","dijit/layout/BorderContainer","dojox/layout/TableContainer","net/sf/xmlform/dojo/Tree","dijit/Tree","dojo/_base/array","dojo/_base/declare"],
		function(sodium,BasePage,ValidationTextBox,BorderContainer,TableContainer,Tree,Tree2,DojoArray,declare){
	
		var ts=["<div class=\"dijitTreeNode\" role=\"presentation\"",
		        "	><div data-dojo-attach-point=\"rowNode\" class=\"dijitTreeRow dijitInline\" role=\"presentation\"",
		        "		><div data-dojo-attach-point=\"indentNode\" class=\"dijitInline\"></div",
		        "		><img src=\"${_blankGif}\" alt=\"\" data-dojo-attach-point=\"expandoNode\" class=\"dijitTreeExpando\" role=\"presentation\"",
		        "		/><span data-dojo-attach-point=\"expandoNodeText\" class=\"dijitExpandoText\" role=\"presentation\"",
		        "		></span",
		        "		><span data-dojo-attach-point=\"contentNode\"",
		        "			class=\"dijitTreeContent\" role=\"presentation\">",
		        "			<img src=\"${_blankGif}\" alt=\"\" data-dojo-attach-point=\"iconNode\" class=\"dijitIcon dijitTreeIcon\" role=\"presentation\"",
		        "			/><input type='checkbox' data-dojo-attach-point=\"checkboxNode\" ${_checkboxStatus}/><span data-dojo-attach-point=\"labelNode\" class=\"dijitTreeLabel\" role=\"treeitem\" tabindex=\"-1\" aria-selected=\"false\"></span>",
		        "		</span",
		        "	></div>",
		        "	<div data-dojo-attach-point=\"containerNode\" class=\"dijitTreeContainer\" role=\"presentation\" style=\"display: none;\"></div>",
		        "</div>"];
		var TreeNode=declare([Tree2._TreeNode],{
			templateString:ts.join(""),
			constructor: function(obj){
				if(obj.checked){
					this._checkboxStatus="checked";
				}else{
					this._checkboxStatus="";
				}
			},
			_updateLayout:function(){
				this.inherited(arguments);
				var par=this.getParent();
				if(par){
					par._onChildChecked();
				}
			},
			setChecked:function(ch){
				this._doSetChecked(ch);
				if(this.tree.oneforall==false){
					return;
				}
				var children = this.getChildren();
				DojoArray.forEach(children, function(child){
					child.setChecked(ch);
				}, this);
				var par=this.getParent();
				if(par){
					par._onChildChecked();
				}
			},
			_onChildChecked:function(){
				var all=true;
				var children = this.getChildren();
				DojoArray.forEach(children, function(child){
					if(child.isChecked()==false)
						all=false;
				}, this);
				this._doSetChecked(all);
				var par=this.getParent();
				if(par&&par._onChildChecked){
					par._onChildChecked();
				}
			},
			_doSetChecked:function(ch){
				this.checkboxNode.checked=ch;
			},
			isChecked:function(){
				return this.checkboxNode.checked;
			}
		});
		var CheckBoxTree=declare([Tree],{
			oneforall:true,
			_createTreeNode: function(args){
				args.checked=this.model.isChecked(args.item);
				return new TreeNode(args);
			},
			_onClick: function(nodeWidget,e){
				this.inherited(arguments);
				nodeWidget.setChecked(!nodeWidget.isChecked());
			},
			_onChildChecked:function(){
				
			}
		});
		CheckBoxTree._TreeNode=TreeNode;

	return sodium.declare(BasePage,{
		onCreateConfig:null,
		onCreatePage:function(){
			this.roleNameFiled=new ValidationTextBox({label: 'name',region:"top",placeholer:"aaaa"});
			var tab=new TableContainer({cols:1,region:"top"});
			tab.addChild(this.roleNameFiled);
			this.treeBox=new CheckBoxTree({region:"center",keyfield:"id",textfield:"text",subform:"children",autoload:false,leaffield:"leaf",leafvalue:true});
			this.treeBox.region="center";
			this.roleForm = new BorderContainer({});
			this.roleForm.addChild(tab);
			this.roleForm.addChild(this.treeBox);
			return this.roleForm;
		},
		onCustomAction:function(cfg){
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
		getCheckedActions:function(node,actions){
			if(node.isChecked()){
				actions.push(node.item);
			};
			var children = node.getChildren();
			for(var i=0;i<children.length;i++){
				this.getCheckedActions(children[i],actions);
			}
		},
		onSaveAction : function() {
			var name=this.roleNameFiled.getValue();
			if(name.length==""){
				this.showErrorMsg("请填写角色名称");
				return;
			}
			
			var actions=[];
			this.getCheckedActions(this.treeBox.rootNode,actions);
			if(actions.length==0){
				this.showErrorMsg("请选择功能权限!");
				return;
			}
			var param={id:this.roleId,name:name};
			var acts=[];
			for(var i=0;i<actions.length;i++){
				acts.push({name:actions[i].id,leaf:actions[i].leaf});
			};
			param.actions=acts;
			this.loadData("admin.role.Save",{save:true},{id:this.toJson(param)});
		},
		onLoadDataComplete:function(cbd,data){
			if(cbd&&cbd.save){
				if(data.faultcode!="ok"){
					this.showErrorMsg(data.head.faultstring);
					return;
				}
				this.superMethod(arguments);
				var rows=data.body.data;
				this.roleId=rows[0][2];
				//this.loadData("admin.role.Read",{},{id:this.roleId});
				this.fireEvent("saveRecord");
				this.showInfoMsg( data.head.faultstring);
			}else{
				var rows=data.body.data;
				var role=this.fromJson(rows[0][2]);
				this.roleNameFiled.setValue(role.name);
				this.treeBox.setRootItems(role.actions);
			}
		},
		onResetPage:function(){
			this.roleNameFiled.setValue("");
			this.resetAllPageForm();
		},
		onOpenPage:function(data){
			var id="";
			if(data!=null&&data.id){
				this.roleId=data.id;
				id=data.id;
			}
			this.loadData("admin.role.Read",{},{id:id});
		}
	});
});