# MyFirstDemo  

 一个简单的使用retrofit+rxjava+okhttp 请求网络数据的Demo  
 ### request example  
 
 ```java  
 Api.getInstance().service.getMsg(10, index)  
                .subscribeOn(Schedulers.io())  
                .observeOn(AndroidSchedulers.mainThread())  
                .map(bean -> results = bean.getResults())  
                .subscribe(results -> setAdapter(results)  
                        , throwable -> ToastUtil.showToast(MainActivity.this, "网络错误"));  
 ```  
 ### gank.io接口不可用了，demo跑不起来了。。
        
        


