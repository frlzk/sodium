sodium.page.CreatePageWindow=function(mainMenu,mainPanel,configParams){
	function menuHandler(page){
		sodium.page.MainMenuHandler(page.targetPage);
	};
	function createMenuData(menu){
		var mitems=[];
		for(var i=0;i<menu.length;i++){
			if(menu[i].page){
				mitems.push({text:menu[i].label,handler:menuHandler,targetPage:menu[i].page});
			}else if(menu[i].children){
				mitems.push({text:menu[i].label,menu:createMenuData(menu[i].children)});
			}
		}
		return mitems;
	};
	var bigPanel={xtype:"panel",
			region:"center",
			layout:"border",
			frame:false,
			border:false,
			bodyBorder:false,
			items:[mainPanel]
	};
	if(mainMenu!=null){
		var menu={
				region:"north",
				autoHeight:true,
				border:false,
				xtype:"toolbar",
				enableOverflow:true,
				items:createMenuData(mainMenu)
			};
		bigPanel.items.push(menu);
	}
	var viewCfg={
		layout:"fit",
		items:[bigPanel]
	};
	return new Ext.Viewport(viewCfg);
};