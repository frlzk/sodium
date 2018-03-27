require(["dojo/domReady!"],function(){
	require(["sodium/window","dijit/layout/BorderContainer","dijit/Menu","dijit/MenuItem","dijit/MenuBar","dijit/MenuBarItem","dijit/PopupMenuBarItem","dijit/PopupMenuItem","dojo/dom-construct","dijit/form/Button"],
		function(swin,BorderContainer,Menu,MenuItem,MenuBar,MenuBarItem,PopupMenuBarItem,PopupMenuItem,construct,Button){
		swin=swin.getInstance();	
		var mainMenu=swin.menu,mainPanel=swin.panel,configParams=swin.params;
			function menuClick(menu){
				swin.openPage(this.get("page"),{single:true});
			};
			function createMenu(){
				var fun=function(evt){
					var item = registry.getEnclosingWidget(evt.target);
					openFun(item.get("page"));
				};
				var menuBar = new MenuBar({region:'top','className':'menuBg'});
				for(var i=0;i<mainMenu.length;i++){
					var mc=mainMenu[i];
					var menus = new Menu({});
					if(mc.children){
						for(var c=0;c<mc.children.length;c++){
							menus.addChild(createMenuItem(mc.children[c]));
						}
					}
					var bi=new PopupMenuBarItem({
						label:mc.label,popup:menus
					});
					menuBar.addChild(bi);
				}
				return menuBar;
			};
			function createMenuItem(item){
				var menu=null;
				if(item.children){
					var pSubMenu = new Menu();
					menu=new PopupMenuItem({
			            label: item.label,
			            popup: pSubMenu
			        });
					for(var c=0;c<item.children.length;c++){
						pSubMenu.addChild(createMenuItem(item.children[c]));
					}
				}else{
					menu=new MenuItem({
						label: item.label,page:item.page,
						onClick: menuClick
					});
				}
				return menu;
			}
			var div = construct.create("div", {id:"_mainViewPortDiv",style:"width:100%;height:100%"},document.body);
			var viewport=new BorderContainer({design: "headline",gutters:false},"_mainViewPortDiv");
			if(mainMenu!=null&&mainMenu.length>0){
				var mb=createMenu();
				viewport.addChild(mb);
			}
			mainPanel.region="center";
			viewport.addChild(mainPanel);
			viewport.startup();
	});
});
