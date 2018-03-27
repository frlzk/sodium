define("sodium/TestRunner",["dojo/_base/declare","dijit/layout/ContentPane","dijit/layout/BorderContainer",
                            "dijit/form/Button","dijit/form/ValidationTextBox","dojox/layout/TableContainer",
                            "dojo/Deferred","dojo/request/xhr","xmlform/model"],
			function(declare,ContentPane,BorderContainer,Button,TextBox,TableContainer,Deferred,xhr,xmodel){
	/*
	 * 
	 */
 	return declare("sodium.TestRunner",[BorderContainer],{
 		postCreate:function(){
 			this.inherited(arguments);
 			var me=this;
 			window.onSodiumPageBoxLoaded=function(box){me.onIframeLoaded(box);};
 			var addr=new TableContainer({showLabels:false,cols:4,style:"width:100%"});
 			var tc="";
 			if(this.testCase&&this.testCase.length>0&&this.testCase!="null"){
 				tc=this.testCase;
 			}
 			this.addrTextBox=new TextBox({style:"width:99%",value:tc});
 			this.testStartBtn=new Button({label:"Start Auto Test",onClick:function(){me.startTest();}});
 			this.testStepBtn=new Button({label:"Start Step Test",onClick:function(){me.startStep();}});
 			this.nextStepBtn=new Button({label:"Next Step",onClick:function(){me.nextStep();},disabled:true});
 			addr.addChild(this.addrTextBox);
 			addr.addChild(this.testStartBtn);
 			addr.addChild(this.testStepBtn);
 			addr.addChild(this.nextStepBtn);
 			addr.region="top";
 			this.addChild(addr);
 			
 			this.iframeBox=new ContentPane({});
 			this.iframeBox.region="center";
 			this.addChild(this.iframeBox);
 			
 			this.infoBox=new ContentPane({style:"width:200px"});
 			this.infoBox.region="right";
 			this.addChild(this.infoBox);
 			
 			this.stepResolve=null;
 			this.testMode="auto";
 			this.initCounter();
 			
 			var me=this;
 			this.tc={
				createPromise:function(fun){
					var d=new Deferred();
					var resolve=function(v){
						d.resolve(v);
					};
					var reject=function(v){
						d.reject(v);
					};
					window.setTimeout(function(){fun(resolve,reject);},1);
					return d;
				},
				data:{},
				openWindow:function(u){
					return this.createPromise(function(resolve, reject){
						me._openWindowResolve=resolve;
						me.iframeBox.set("content","<iframe src='"+me.baseServletPath+"/window/"+u+"' width='100%' height='100%' frameborder='no' border='0'></iframe>");
					});
				},
				runTest:function(T){
					return this.createPromise(function(resolve, reject){
						me.runTestCase(T,resolve);
					});
				},
				getPageBox:function(){
					return me.rootPageBox;
				},
				waitState:function(tf){
					return this.createPromise(function(resolve, reject){
						var f=function(){
							if(tf()){
								resolve();
								return;
							}
							window.setTimeout(f,100);
						};
						window.setTimeout(f,100);
					});
				},
				
				executeAction:function(action,param){
					var data=param;
					if(!data.head||!data.version){
						data=xmodel.createFormData(param);
					}
					return xhr(me.baseServletPath+"/action/"+action,{data:dojo.toJson(data),handleAs: "json",method:"post"})
				},
				syncExecuteAction:function(action,param){
					var data=param;
					if(!data.head||!data.version){
						data=xmodel.createFormData(param);
					}
					var res=null;
					xhr(me.baseServletPath+"/action/"+action,{data:dojo.toJson(data),handleAs: "json",method:"post",sync:true}).then(function(r){
						res=r;
					});
					return res;
				},
				setFormFieldValues:function(page,form,fieldPath,values){
					for(var k in values){
						fieldPath.push(k);
						var res=this.setFormFieldValue(page,form,fieldPath,values[k]);
						if(res==false){
							return false;
						}
						fieldPath.pop();
					}
					return true;
				},
				setFormFieldValue:function(page,form,fieldPath,value){
					var rs=page.getRecordSet(form);
					if(rs==null){
						return false;
					}
					var field=rs.getRecordField(fieldPath);
					if(field==null){
						return false;
					}
					field.setValue(value);
					return true;
				},
				getFormFieldValue:function(page,form,fieldPath){
					var rs=page.getRecordSet(form);
					if(rs==null){
						throw new Error("Not found Recordset: "+form);
					}
					var field=rs.getRecordField(fieldPath);
					if(field==null){
						throw new Error("Not found field: "+fieldPath);
					}
					if(field.hasText()){
						return [field.getValue(),field.getText()];
					}
					return field.getValue();
				},
				getFormField:function(page,form,fieldPath){
					var rs=page.getRecordSet(form);
					if(rs==null){
						throw new Error("Not found Recordset: "+form);
					}
					var field=rs.getRecordField(fieldPath);
					if(field==null){
						throw new Error("Not found field: "+fieldPath);
					}
					return field;
				},
				triggerPageAnchor:function(page,param){
					if(me.testMode=="step"){
						return this.createPromise(function(resolve, reject){
							me.stepResolve=resolve;
							me.setStepEnable();
						}).then(function(){
							return page.triggerPageAnchor(param);
						});
					}
					return page.triggerPageAnchor(param);
				},
				triggerOpenWindowAnchor:function(page,param){
					return this.createPromise(function(resolve, reject){
						me._openWindowResolve=function(e){
							resolve(e);
						};
						page.triggerPageAnchor(param);
					});
				},
				
				assertEquals:function(){
					if(arguments.length==2){
						me.increateAssert(arguments[0]==arguments[1]?null:"Expect "+arguments[0]+",but "+arguments[1]);						
					}else{
						me.increateAssert(arguments[1]==arguments[2]?null:arguments[0]+" Expect "+arguments[1]+",but "+arguments[2]);		
					}
				}
 			};
 			if(this.initData){
 				this.tc.data=this.initData;
 			}
 			if(tc.length>0){
 				window.setTimeout(function(){me.startTest();},1); 				
 			}
 		},
 		onIframeLoaded:function(box){
 			this.rootPageBox=box;
 			var b={iframe:this.iframeBox.get("content"),pageBox:box};
 			if(box.setAutoConfirmMessage){
 				box.setAutoConfirmMessage(true);
 			}
 			this._openWindowResolve(b);
 		},
 		initCounter:function(){
 			this.testTotal=0;
 			this.testSuccess=0;
 			this.testFault=0;
 			this.testStack=[];
 			this.testErrs=[];
 			this.stepResolve=null;
 		},
 		setStepEnable:function(){
 			this.nextStepBtn.set("disabled",false);
 		},
 		nextStep:function(){
 			this.nextStepBtn.set("disabled",true);
 			if(this.stepResolve!=null){
 				this.stepResolve();
 			}
 		},
 		startStep:function(){
 			this.stepCase="";
 			this._startTest("step");
 		},
 		startTest:function(){
 			this._startTest("auto");
 		},
 		_startTest:function(m){
 			this.testMode=m;
 			this.rootPageBox=null;
 			var me=this;
 			me.tc.openWindow(me.firstPage).then(function(evt){
 				me.doStartTest();
 			});
 		},
 		doStartTest:function(){
 			this.initCounter();
 			this.startTime=new Date().getTime();
 			var testCase=this.addrTextBox.get("value");
 			if(testCase==null||testCase.length==0){
 				return;
 			}
 			this.updateInfo();
 			var me=this;
 			this.runTestCase(testCase,function(){
 				me.emit("testcomplete",{testCase:testCase,
 					total:me.testTotal,
 					successes:me.testSuccess,
 					failures:me.testFault
 				});
 			});
 		},
 		increateAssert:function(err){
 			this.testTotal++;
 			if(err==null){
 				this.testSuccess++;
 				this.updateInfo();
 				return;
 			}
 			this.testFault++;
 			var tet=this.testStack[this.testStack.length-1];
 			this.testErrs.push(tet.testcase+"-"+tet.funs[0]+": "+err);
 			this.updateInfo();
 		},
 		runTestCase:function(c,resolve){
 			var me=this,method=null;
 			var tc=c.split("-");
 			if(tc.length>1){
 				method=tc[1];
 			}
 			if(me.stepCase&&me.stepCase.indexOf("-")<0){
	 			if(me.stepCase==tc[0]){
	 				this.testMode="step";
	 				console.log("Begin into step test: "+me.stepCase);
	 			}
 			}
 			require([tc[0].replace(/\./g,"/")],function(T){me._beginRun(tc[0],resolve,T,method);});
 		},
 		_beginRun:function(c,resolve,T,method){
 			var funs=[];
 			if(method&&T[method]){
 				funs.push(method);
 			}else{
	 			for(var k in T){
	 				if(k.indexOf("test")==0&&k.length>"test".length){
	 					funs.push(k);
	 				}
	 			}
 			}
 			funs.sort();
 			if(funs.length>0){
 				this.testStack.push({testcase:c,resolve:resolve,funs:funs});
 				T.tc=this.tc;
 				this._done2(T); 				
 			}else{
 				resolve();
 			}
 		},
 		_done1:function(T){
 			var me=this;
 			return function(){
 				window.setTimeout(function(){
 					var tet=me.testStack[me.testStack.length-1];
 					tet.funs.shift();
 					me._done2(T);
 				},1);
 			};
 		},
 		_done2:function(T){
 			var tet=this.testStack[this.testStack.length-1];
 			if(tet.funs.length==0){
 				this.testStack.pop().resolve();
				this.updateInfo();
				T.tc=null;
				return;
 			}
 			this.updateInfo();
 			var f=tet.funs[0];
 			if(this.stepCase&&this.stepCase.indexOf("-")>0){
 				var sc=this.stepCase.split("-");
	 			if(sc[0]==tet.testcase&&sc[1]==f){
	 				this.testMode="step";
	 				console.log("Begin into step test: "+this.stepCase);
	 			}
 			}
 			T[f].apply(T,[this._done1(T)]);
 		},
 		updateInfo:function(){
 			var html=["Tests run: ",this.testTotal,
 			          "<br>&nbsp;&nbsp;Successes: ",this.testSuccess,
 			          "<br>&nbsp;&nbsp;Failures: ",this.testFault,
 			          "<br>&nbsp;&nbsp;Times: ",((new Date().getTime()-this.startTime)/1000),"s<br>"];
 			for(var i=0;i<this.testStack.length;i++){
 				html.push("<span style='white-space:nowrap'>");
 				html.push(this.testStack[i].testcase+"-"+this.testStack[i].funs[0]);
 				html.push("</span><br/>");
 			}
 			html.push("<br/>Failures:<br/>");
 			for(var i=0;i<this.testErrs.length;i++){
 				html.push("<span style='white-space:nowrap'>");
 				html.push(this.testErrs[i]);
 				html.push("</span><br/>");
 			}
 			this.infoBox.set("content",html.join(""));
 		}
 	});
 });