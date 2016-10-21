package com.honestwalker.android.APICore.config;

import android.content.Context;

import com.honestwalker.android.APICore.exception.ServerConfigException;
import com.honestwalker.android.fastroid.http.R;
import com.honestwalker.androidutils.ClassUtil;
import com.honestwalker.androidutils.IO.LogCat;
import com.honestwalker.androidutils.exception.ExceptionUtil;

import org.jdom.Attribute;
import org.jdom.Document;
import org.jdom.Element;
import org.jdom.JDOMException;
import org.jdom.input.SAXBuilder;

import java.io.IOException;
import java.io.InputStream;
import java.lang.reflect.Field;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

/**
 * Server 配置读取对象
 * Created by honestwalker on 15-12-24.
 */
public final class ServerLoader {

    private static String TAG = "ServerConfig";

    private static ServerConfig config = null;

    private ServerLoader(){}

    /**
     * 读取服务端配置
     * @param context
     * @throws JDOMException
     * @throws IOException
     */
    private static <T extends ServerContextSupport> void loadServerConfig(Context context , Class<T> clazz, int serverConfigRes) throws ServerConfigException {

        try {
            config = new ServerConfig();

            InputStream in = context.getResources().openRawResource(serverConfigRes);
            SAXBuilder sb = new SAXBuilder();
            Document doc = sb.build(in);//读入指定文件
            Element root = doc.getRootElement();//获得根节点
            List<Element> list = root.getChildren();//将根节点下的所有子节点放入List中
            for(Element elm : list) {
                if(elm.getName().equals("scheme")) {
                    loadScheme(elm);
                } else if(elm.getName().equals("contexts")) {
                    loadContexts(elm , clazz);
                }
            }

        } catch (Exception e) {
            ExceptionUtil.showException("REQUEST" , e);
            throw new ServerConfigException("Server配置初始化失败 . ");
        }

    }

    /**
     * 读取当前服务端方案配置
     * @param elm 方案节点
     */
    private static void loadScheme(Element elm) {
        Element contextIDELM = elm.getChild("context-id");
//        Scheme scheme = new Scheme();
        String contextID = contextIDELM.getValue();
//        scheme.setContextID(contextID);
        config.setScheme(contextID);
    }

    /**
     * 读取服务端环境列表配置
     * @param elm 服务端列表节点
     */
    private static <T extends ServerContextSupport> void loadContexts(Element elm , Class<T> contextClass) throws ServerConfigException {

        try {

            List<Element> contextELMList = elm.getChildren("context");

            for(Element contextELM : contextELMList) {

                // context配置随意定义，反射机制与对象绑定

                Object contextObj = contextClass.newInstance();
                try {

                    /// 读取子节点
                    List<Element> children = contextELM.getChildren();
                    LogCat.d(TAG , contextELM.getName() + " : ");
                    for(Element child : children) {
                        LogCat.d(TAG , contextObj.getClass().getSimpleName() + " 找 " +
                                child.getName() + " " +
                                (ClassUtil.hasField(contextObj.getClass() , child.getName() , true)));
                        if(ClassUtil.hasField(contextObj.getClass() , child.getName() , true)) {
                            Field field = ClassUtil.getField(contextObj.getClass() , child.getName() , true);
                            field.setAccessible(true);
                            field.set(contextObj, child.getValue());
                            LogCat.d(TAG, "设置" + child.getName() + "=" + child.getValue());
                        }
                    }

                    /// 读取属性
                    List<Attribute> attributes = contextELM.getAttributes();
                    for(Attribute attribute : attributes) {
                        LogCat.d(TAG, contextObj.getClass().getSimpleName() + " 找 " +
                                attribute.getName() + " " +
                                (ClassUtil.hasField(contextObj.getClass(), attribute.getName(), true)));
                        if("extends".equals(attribute.getName()) || ClassUtil.hasField(contextObj.getClass() , attribute.getName() , true)) {

                            String attributeName = "";

                            if("extends".equals(attribute.getName())) { // extends 是关键字 ， 自动设置成parent_id
                                attributeName = "parent_id";
                            } else {
                                attributeName = attribute.getName();
                            }

                            Field field = ClassUtil.getField(contextObj.getClass(), attributeName, true);
                            field.setAccessible(true);
                            field.set(contextObj, attribute.getValue());
                            LogCat.d(TAG, "设置" + attribute.getName() + "=" + attribute.getValue());

                        }
                    }
                    config.getServerContexts().put(contextELM.getAttribute("id").getValue() , contextObj);
                    LogCat.d(TAG, "添加" + contextELM.getAttribute("id") );
                    LogCat.d(TAG, " ----- ");

                } catch (Exception e) {
                    ExceptionUtil.showException(e);
                }

            }

//            checkExtends();
            extendsData();

        } catch (Exception e) {
            ExceptionUtil.showException("REQUEST" , e);
            throw new ServerConfigException("Server配置初始化失败");
        }

    }

    /**
     * 处理继承关系
     * @param <T>
     */
    private static <T extends ServerContextSupport> void extendsData() {

        ServerContextTreeList serverContextTreeList = new ServerContextTreeList();  // 创建环境对象树列表 ， 用语对象关系处理操作
        HashMap<String , T> serverContextMapping    = config.getServerContexts();   // 获得所有环境 ， 准备处理
        Iterator<Map.Entry<String , T>> iter        = serverContextMapping.entrySet().iterator();

        /// 创建对象继承关系树
        while(iter.hasNext()) {

            Map.Entry<String , T> ent = iter.next();
            String id = ent.getKey();
            T value = ent.getValue();

            ServerContextTree treeNode = serverContextTreeList.getServerContextTreeMapping().get(id);
            if(treeNode != null) {

                /// 树节点已经创建，赋值，因为创建树节点，并不一定赋予数据， 他是遍历到子节点时创建的，遍历到当前类时才会在 serverContextTreeList 中存在
                // 设置节点数据
                treeNode.setServerContext(value);

                if(value.getParent_id() == null) {     // 当前节点没有parent 它是树顶

                    treeNode.setServerContext(value);
                    serverContextTreeList.getServerContextTreeMapping().put(id, treeNode);
                    serverContextTreeList.getRootServerContextTree().put(id , treeNode);   // 添加到Root表里

                } else {
                    /// 子树 ， 查找或创建父节点 ， 并添加到父节点的child

                    //  serverContextTreeList.getServerContextTreeMapping().get(id).getChild().put(id , serverContextTree);
                    ServerContextTree parentNode = serverContextTreeList.getServerContextTreeMapping().get(value.getParent_id());

                    /// 如果找到父节点， 把父节点的child添加当前节点
                    /// 如果找不到，创建无值父节点， 并且child添加当前节点
                    if(parentNode != null) {
                        parentNode.getChild().put(id, treeNode);
                    }  else {
                        // 如果父节点还没有创建，创建没有值的父节点 ， 因为此时不知道他的值 ， 只有遍历到父节点后才赋值
                        parentNode = new ServerContextTree();
                        parentNode.getChild().put(id , treeNode);
                        serverContextTreeList.getServerContextTreeMapping().put(value.getParent_id() , parentNode);
                    }
                    serverContextTreeList.getServerContextTreeMapping().put(id, treeNode);
                }

            } else {

                // 创建树顶
                treeNode = new ServerContextTree();
                treeNode.setServerContext(value);

                if(value.getParent_id() == null) {  // 没有parent_id 说明是树顶节点 ， 创建树顶节点

                    serverContextTreeList.getServerContextTreeMapping().put(id , treeNode);

                    serverContextTreeList.getRootServerContextTree().put(id , treeNode);

                } else {

                    /// 子树 ， 查找或创建父节点 ， 并添加到父节点的child

                    ServerContextTree parentNode = serverContextTreeList.getServerContextTreeMapping().get(value.getParent_id());

                    /// 如果找到父节点， 把父节点的child添加当前节点
                    /// 如果找不到，创建无值父节点， 并且child添加当前节点
                    if(parentNode != null) {
                        parentNode.getChild().put(id, treeNode);
                    }  else {
                        // 如果父节点还没有创建，创建没有值的父节点 ， 因为此时不知道他的值 ， 只有遍历到父节点后才赋值
                        parentNode = new ServerContextTree();
                        parentNode.getChild().put(id , treeNode);
                        serverContextTreeList.getServerContextTreeMapping().put(value.getParent_id() , parentNode);
                    }

                }

                // 添加当前节点到映射表
                serverContextTreeList.getServerContextTreeMapping().put(id , treeNode);

            }

        }

        // 遍历树 ， 赋值数据
        HashMap<String , ServerContextTree> rootServerContextTree = serverContextTreeList.getRootServerContextTree();
        for(ServerContextTree tree : rootServerContextTree.values()) { // 遍历每棵树 ， 向下拷贝数据
            copyExtendData(tree);
        }

    }

    /**
     * 递归拷贝继承数据
     * @param tree  当前节点
     */
    private static void copyExtendData(ServerContextTree tree) {
        ServerContextSupport serverContext = tree.getServerContext();

        if(tree.getChild() == null) return;

        HashMap<String , ServerContextTree> childTree = tree.getChild();
        for(ServerContextTree child : childTree.values()) {
            ClassUtil.reflectCopy(serverContext , child.getServerContext() , false , false);
            copyExtendData(child);
        }
    }

    /**
     * 拷贝父类数据
     * @param context
     * @param parentContext
     */
    private static <T extends ServerContextSupport> void loadParentData(T context , T parentContext) {
        LogCat.d("ServerConfig" , "拷贝 " + parentContext + "@" + parentContext.getId() + " 到 " + context + "@" + context.getId());
        ClassUtil.reflectCopy(parentContext, context, false, false);
        if(parentContext.getParent_id() != null) {
            T superContext = (T) config.getServerContexts().get(parentContext.getParent_id());
            try {
                loadParentData(superContext , context);
            } catch (Exception e) {
                ExceptionUtil.showException("SERVER" , e);
            }
        }
    }

//    private final static ArrayList<String> checkExtendsTmp = new ArrayList<>();
//    private static <T extends ServerContextSupport> boolean checkExtends() {
//        checkExtendsTmp.clear();
//        for(Object context : config.getServerContexts().values()) {
//            checkExtendsTmp.add( ((ServerContextSupport)context).getId() ) ;
//        }
//    }
//
//    private static <T extends ServerContextSupport> boolean checkExtends(T context) {
//        checkExtendsTmp.clear();
//        checkExtendsTmp.add(context.getId());
//        return checkParentExtends(context);
//    }
//
//    private static <T extends ServerContextSupport> boolean checkParentExtends(T context) {
//        String parentID = context.getParent_id();
//        if(parentID == null) return true;
//
//        if(checkExtendsTmp.contains(parentID)) return false;    // 如果有循环继承 返回false
//
//        checkExtendsTmp.add(parentID);
//        T parentContext = (T) config.getServerContexts().get(parentID);
//        return checkParentExtends(parentContext);
//    }

    /**
     * 读取server配置
     * @param context
     * @param sercerContextClass
     * @param <T>
     * @return
     * @throws ServerConfigException
     */
    public static <T extends ServerContextSupport> ServerConfig<T> getServerConfig(Context context , Class<T> sercerContextClass, int serverConfigRes) throws ServerConfigException {
        if(config == null) {
            loadServerConfig(context , sercerContextClass, serverConfigRes);
        }
        return config;
    }

}
