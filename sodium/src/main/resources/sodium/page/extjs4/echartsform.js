/**
 * @author Liu Zhikun
 */
Ext.define("sodium.page.ChartForm",{
	layout:"fit",
	extend:"Ext.panel.Panel",
	constructor:function(config){
		if(typeof(this["_createCharType"+config.chart.type])=="undefined"){
			throw new Error("Not support chart type: "+obj.type);
		}
		this.chartConfig=config.chart;
		this._echartId="ec"+sodium.page._nextId();
		config.html="<div id='"+this._echartId+"' style='width:100%;height:100%'></div>";
		this.callParent(arguments);
		this.chartConfig.xmlformRecordset.addListener(this,this._onRecordSetUpdate);
	},
	initComponent:function(){
		this.callParent(arguments);
	},
	afterRender:function(){
		this.callParent(arguments);
	},
	getXmlformRecordset:function(){
		return this.chartConfig.xmlformRecordset;
	},
	_onRecordSetUpdate:function(evt){
		if(evt.type!="update"){
			return;
		}
		this.refresh();
	},
	refresh:function(){
		Ext.getDom(id).innerHTML="";
		this["_createCharType"+this.chartConfig.type](this.chartConfig.xmlformRecordset.getRecords());
		//this.layout();
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
	_createCharTypepie:function(rs){
		this._createEcharts("pie",rs);
	},
	_createEcharts:function(type,rs,toolbox){
		var option=this._createOptions(type,rs,toolbox);
		var myChart = Echarts.init(document.getElementById(this._charPanId));
        myChart.setOption(option);
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
		var yarray=	this.chartConfig.y;
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
			var xfield=rs[i].getField(this.chartConfig.x.field);
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
		if(rs.length>1||(rs.length>0&&this.chartConfig.y.length>1)){
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
			var d=net.sf.xmlform.val.parseDate(field.getValue());
			if(d==null){
				return null;
			}
			return parseInt(d.getTime()/1000/60/60/24,10);
		}else if(field.getType()=="datetime"){
			var d=net.sf.xmlform.val.parseDateTime(field.getValue());
			if(d==null){
				return null;
			}
			return parseInt(d.getTime()/1000/60/60/24,10);
		}else{
			return field.getValue();
		}
	}
});