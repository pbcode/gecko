# gecko(为了了解dubbo相关原理自己实现学习所用)<br/>

自己写的RPC框架，丰富她<br/>

没有分层与设计模式的参与，诚待整理<br/>


1、spring无缝链接，无xml配置文件<br/>
2、服务发现透明化<br/>
3、服务调用透明化<br/>
4、简易序列化 <br/>
5、软负载支持随机权重以及轮询  <br/>
6、两种fairOver模式    <br>
7、服务治理（待开放）   <br>
8、服务分组（待开放）   <br>
9、扩展性？（待开放）   <br>
10、异步调用（待开放）   <br>


修改了ZK闪断问题通过ZKClient的超时时间延长
新增了服务端自发现
