# gecko<br/>

自己写的RPC框架，丰富她<br/>

比较乱，诚待整理<br/>


1、spring无缝链接，无xml配置文件<br/>
2、服务发现透明化<br/>
3、服务调用透明化<br/>
4、简易序列化 <br/>
5、软负载（待开放，仅有随机）  <br/>
6、两种fairOver模式    <br>
7、服务治理（待开放）   <br>
8、服务分组（待开放）   <br>
9、扩展性？（待开放）   <br>
10、异步调用（待开放）   <br>



brpc是一个基于protobuf接口的RPC框架，在百度内部称为“baidu-rpc”，它囊括了百度内部所有RPC协议，并支持多种第三方协议，从目前的性能测试数据来看，brpc的性能领跑于其他同类RPC产品。   <br>
Dubbo是Alibaba开发的一个RPC框架，远程接口基于Java Interface, 依托于Spring框架。   <br>
gRPC的Java实现的底层网络库是基于Netty开发而来，其Go实现是基于net库。   <br>
Thrift是Apache的一个项目(http://thrift.apache.org)，前身是Facebook开发的一个RPC框架，采用thrift作为IDL (Interface description language)。   <br>
jsonrpc   <br>


基础框架：Netty - Netty框架不局限于RPC，更多的是作为一种网络协议的实现框架，比如HTTP，由于RPC需要高效的网络通信，就可能选择以Netty作为基础。   <br>
