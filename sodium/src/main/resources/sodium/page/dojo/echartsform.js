/**
 * @author Liu Zhikun
 */
define("sodium/page/ChartForm",["dojo/_base/declare","xmlform/dojo/BorderContainer","dijit/layout/ContentPane","xmlform/panel","dojo/dom-class","dojo/dom","xmlform/dojo/Div","xmlform/dojo/LayoutUtil",'echarts'
                        ], function(declare,BorderContainer,ContentPane,xmlformPanel,DomClass,dojoDom,Div,LayoutUtil,Echarts
                       ){
	return declare("sodium.page.ChartForm",[BorderContainer],{
		gutters:false,
		constructor: function(obj){
			/* 
			 * type:"lines"
			 * x:{field}
			 * y:[{field,color,}...]
			 * 
			 * type:"columns"
			 * x:{field}
			 * y:[{field,color,}...]
			 * 
			 *  * type:"pie"
			 * x:{field}
			 * y:[{field,color,label:value|percent}...]
			 */
			if(typeof(this["_createCharType"+obj.type])=="undefined"){
				throw new Error("Not support chart type: "+obj.type);
			}
			this.charConfig=obj;
			this.charConfig.xmlformRecordSet.addListener(this,this._onRecordSetUpdate);
		},
		postCreate:function(){
			this._capHt=0;
			if(this.caption){
				var cap=new Div({html:this.caption});
				cap.region="top";
				DomClass.add(cap.domNode,xmlformPanel.getFormCaptionCls(null));
				this.addChild(cap);
				this._capHt=LayoutUtil.getToolbarHeight();
			}
			this._charPanId=this.id+"_char";
			this._charPan=new ContentPane({content:"<div id='"+this._charPanId+"' style='width:100%;height:100%'></div>"});
			this._charPan.region="center";
			this.addChild(this._charPan);
			var me=this;
			this._charPan.getSizePolicy=function(){
				return me.getSizePolicy();
			};
		},
		getXmlformRecordset:function(){
			return this.charConfig.xmlformRecordSet;
		},
		getSizePolicy:function(){
			var size={
					halign:this.halign||"center",
					valign:this.valign||"middle",
					hstretch:this.hstretch||0,
					vstretch:this.vstretch||0,
					minwidth:150,
					bestwidth:200,
					minheight:150,
					bestheight:200
				};
			var cc=this.charConfig;
			if(cc.minwidth){
				size.minwidth=cc.minwidth;
			}
			if(cc.bestwidth){
				size.bestwidth=cc.bestwidth;
			}
			if(cc.minheight){
				size.minheight=cc.minheight;
			}
			if(cc.bestheight){
				size.bestheight=cc.bestheight;
			}
			size.minheight+=this._capHt;
			size.bestheight+=this._capHt;
			return size;
		},
		_onRecordSetUpdate:function(evt){
			if(evt.type!="update"){
				return;
			}
			this.refresh();
		},
		refresh:function(){
			if(!this._echars){
				this._echars=Echarts.init(dojoDom.byId(this._charPanId));
			}
			this["_createCharType"+this.charConfig.type](this.charConfig.xmlformRecordSet.getRecords());
			this.layout();
		},
		_createCharTypelines:function(rs){
			this._createEcharts("line",rs,['line', 'bar']);
		},
		_createCharTypecurve:function(rs){
			this._createEcharts("line",rs,['line', 'bar']);
		},
		_createCharTypecolumns:function(rs){
			this._createEcharts("bar",rs,['line', 'bar']);
		},
		_createCharTypevcolumns:function(rs){
			this._createEcharts("bar",rs,[],{exchangexy:true});
		},
		_createCharTypepie:function(rs){
			this._createEcharts("pie",rs);
		},
		_createEcharts:function(type,rs,toolbox,opt){
			var option=this._createOptions(type,rs,toolbox);
			if(opt&&opt.exchangexy){
				var v=option.xAxis;
				option.xAxis=option.yAxis;
				option.yAxis=v;
			}
			if(this.charConfig.option){
				for(var k in this.charConfig.option){
					option[k]=this.charConfig.option[k];
				}
			}
			this._echars.setOption(option);
		},
		_createOptions:function(type,rs,toolbox){
			var cfg={
					animation:false,
					tooltip : {show: true,trigger: 'item'},
				    calculable : true,
				    xAxis:[{type : 'category',data : []}],
				    yAxis : [ {type : 'value',splitArea : {show : true}}],
				    series:[]
			};
			if(toolbox){
				cfg.toolbox={
				        show : true,
				        feature : {
				            mark : {show: false},
				            magicType : {show: true, type:toolbox},
				            saveAsImage : {show: true}
				        }
				    };
			}
			var xlabels=cfg.xAxis[0].data;
			var maxLen=0;
			var legendLabel=[];
			var yarray=	this.charConfig.y;
			for(var y=0;y<yarray.length;y++){
				var ser={name:null,
			            type:type,
			            stack:null,
			            data:[]};
				cfg.series.push(ser);
				legendLabel.push("");
				if(yarray[y].label){
					ser.name=yarray[y].label;
					legendLabel[y]=ser.name;
					if(ser.name.length>maxLen){
						maxLen=ser.name.length;
					}
				}
			}
			for(var i=0;i<rs.length;i++){
				var xfield=rs[i].getField(this.charConfig.x.field);
				xlabels.push(xfield.getText());
				for(var y=0;y<yarray.length;y++){
					var yfield=rs[i].getField(yarray[y].field);
					if(i==0){
						if(!cfg.series[y].name){
							cfg.series[y].name=yfield.getLabel();
							legendLabel[y]=yfield.getLabel();
							if(legendLabel[y].length>maxLen){
								maxLen=legendLabel[y].length;
							}
						}
					}
					var yv=this._getFieldValue(yfield);
					cfg.series[y].data.push(yv);
				}
			}
			if(rs.length>1||(rs.length>0&&this.charConfig.y.length>1)){
				cfg.legend={x:"left",y:"top",data:legendLabel};
			}
			if(rs.length>8){
				cfg.xAxis[0].axisLabel={
                    rotate: 45
                };
				cfg.grid={y2:maxLen*20};
			}
			return cfg;
		},
		_getFieldValue:function(field){
			if(field.getType()=="date"){
				var d=xmlformVal.parseDate(field.getValue());
				if(d==null){
					return null;
				}
				return parseInt(d.getTime()/1000/60/60/24,10);
			}else if(field.getType()=="datetime"){
				var d=xmlformVal.parseDateTime(field.getValue());
				if(d==null){
					return null;
				}
				return parseInt(d.getTime()/1000/60/60/24,10);
			}else{
				return field.getValue();
			}
		}
	});
});