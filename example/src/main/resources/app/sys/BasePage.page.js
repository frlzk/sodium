{//BEGIN_DECLARE
	name:sys.BasePage,
	comment:基本页
}//END_DECLARE

define(["sodium","sodium/BasePage"],function(sodium,BasePage){
	return sodium.declare(BasePage,{
		onCreateConfig:null,
		loadData:function(action,cbd,param,fir,mx){
			var me=this;
			var data=this.objToFormData(param);
			if(fir){
				data.head.firstresult=fir;
			}
			if(mx){
				data.head.maxresults=mx;
			}
			this.callServerAction({action:action,data:data,callctx:cbd})
			.then(function(result){
				me.onLoadDataComplete(result.callctx,result.response);
			});
		},
		onLoadDataComplete:function(cbd,data){
			if(cbd.form){
				var rs=this.getRecordSet(cbd.form);
				if(rs!=null){
					rs.setData(data);
				}
			}
		}
	});
});