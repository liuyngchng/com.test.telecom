<!DOCTYPE html PUBLIC "-//W3C//DTD HTML 4.01 Transitional//EN" "http://www.w3.org/TR/html4/loose.dtd">
<html>
<head>
    <meta http-equiv="Content-Type" content="text/html; charset=UTF-8">
    <meta name="viewport" content="width=device-width, initial-scale=1, shrink-to-fit=no">
    <title>Index</title>
    <link rel="stylesheet" href="/css/demo.css">
    <link rel="stylesheet" href="/css/zTreeStyle/zTreeStyle.css">
    <#--<script type="text/css" src="/css/bootstrap.min.css"></script>-->
    <#--    <script type="application/javascript" src="/js/jquery.ztree.all.js"></script>-->
    <script type="application/javascript" src="/js/jquery-1.4.4.min.js"></script>
    <script type="application/javascript" src="/js/jquery.ztree.core.js"></script>
    <script type="application/javascript" src="/js/jquery.ztree.excheck.js"></script>
    <script type="application/javascript" src="/js/jquery.ztree.exedit.js"></script>
    <script type="text/javascript">
        <!--
        var setting = {
            view: {
                showIcon: showIconForTree,
                selectedMulti: true
            },
            data: {
                simpleData: {
                    enable: true
                }
            },
            edit: {
                enable: true,
                showRemoveBtn: false,
                showRenameBtn: false,
                drag:{
                    isCopy: true,
                    isMove: true,
                    prev: true,
                    inner: true,
                    next: true
                }
            },
            callback: {
                beforeDrag: beforeDrag,
                beforeDrop: beforeDrop,
                beforeClick: beforeClick
            }
        };

        var zNodes =[
            { id:1, pId:0, name:"父节点1 - 展开", open:true},
            { id:11, pId:1, name:"父节点11 - 折叠"},
            { id:111, pId:11, name:"叶子节点111"},
            { id:112, pId:11, name:"叶子节点112"},
            { id:113, pId:11, name:"叶子节点113"},
            { id:114, pId:11, name:"叶子节点114"},
            { id:12, pId:1, name:"父节点12 - 折叠"},
            { id:121, pId:12, name:"叶子节点121"},
            { id:122, pId:12, name:"叶子节点122"},
            { id:123, pId:12, name:"叶子节点123"},
            { id:124, pId:12, name:"叶子节点124"},
            { id:13, pId:1, name:"父节点13 - 没有子节点", isParent:true},
            { id:2, pId:0, name:"父节点2 - 折叠"},
            { id:21, pId:2, name:"父节点21 - 展开", open:true},
            { id:211, pId:21, name:"叶子节点211"},
            { id:212, pId:21, name:"叶子节点212"},
            { id:213, pId:21, name:"叶子节点213"},
            { id:214, pId:21, name:"叶子节点214"},
            { id:22, pId:2, name:"父节点22 - 折叠"},
            { id:221, pId:22, name:"叶子节点221"},
            { id:222, pId:22, name:"叶子节点222"},
            { id:223, pId:22, name:"叶子节点223"},
            { id:224, pId:22, name:"叶子节点224"},
            { id:23, pId:2, name:"父节点23 - 折叠"},
            { id:231, pId:23, name:"叶子节点231"},
            { id:232, pId:23, name:"叶子节点232"},
            { id:233, pId:23, name:"叶子节点233"},
            { id:234, pId:23, name:"叶子节点234"},
            { id:3, pId:0, name:"父节点3 - 没有子节点", isParent:true}
        ];

        function showIconForTree(treeId, treeNode) {
            return !treeNode.isParent;
        };

        function beforeDrag(treeId, treeNodes) {
            for (var i=0,l=treeNodes.length; i<l; i++) {
                if (treeNodes[i].drag === false) {
                    return false;
                }
            }
            return true;
        };

        function beforeDrop(treeId, treeNodes, targetNode, moveType) {
            return targetNode ? targetNode.drop !== false : true;
        };

        function beforeClick(treeId, treeNode) {
            return !treeNode.isCur;
        };

        var curSrcNode, curType;
        function fontCss(treeNode) {
            var aObj = $("#" + treeNode.tId + "_a");
            aObj.removeClass("copy").removeClass("cut");
            if (treeNode === curSrcNode) {
                if (curType == "copy") {
                    aObj.addClass(curType);
                } else {
                    aObj.addClass(curType);
                }
            }
        };

        function setCurSrcNode(treeNode) {
            var zTree = $.fn.zTree.getZTreeObj("treeDemo");
            if (curSrcNode) {
                delete curSrcNode.isCur;
                var tmpNode = curSrcNode;
                curSrcNode = null;
                fontCss(tmpNode);
            }
            curSrcNode = treeNode;
            if (!treeNode) return;

            curSrcNode.isCur = true;
            zTree.cancelSelectedNode();
            fontCss(curSrcNode);
        };

        function copy(e) {
            var zTree = $.fn.zTree.getZTreeObj("treeDemo"),
                nodes = zTree.getSelectedNodes();
            if (nodes.length == 0) {
                alert("请先选择一个节点");
                return;
            }
            curType = "copy";
            setCurSrcNode(nodes[0]);
        }
        function cut(e) {
            var zTree = $.fn.zTree.getZTreeObj("treeDemo"),
                nodes = zTree.getSelectedNodes();
            if (nodes.length == 0) {
                alert("请先选择一个节点");
                return;
            }
            curType = "cut";
            setCurSrcNode(nodes[0]);
        }
        function paste(e) {
            if (!curSrcNode) {
                alert("请先选择一个节点进行 复制 / 剪切");
                return;
            }
            var zTree = $.fn.zTree.getZTreeObj("treeDemo"),
                nodes = zTree.getSelectedNodes(),
                targetNode = nodes.length>0? nodes[0]:null;
            if (curSrcNode === targetNode) {
                alert("不能移动，源节点 与 目标节点相同");
                return;
            } else if (curType === "cut" && ((!!targetNode && curSrcNode.parentTId === targetNode.tId) || (!targetNode && !curSrcNode.parentTId))) {
                alert("不能移动，源节点 已经存在于 目标节点中");
                return;
            } else if (curType === "copy") {
                targetNode = zTree.copyNode(targetNode, curSrcNode, "inner");
            } else if (curType === "cut") {
                targetNode = zTree.moveNode(targetNode, curSrcNode, "inner");
                if (!targetNode) {
                    alert("剪切失败，源节点是目标节点的父节点");
                }
                targetNode = curSrcNode;
            }
            setCurSrcNode();
            delete targetNode.isCur;
            zTree.selectNode(targetNode);
        }

        function setCheck() {
            var isCopy = setting.edit.drag.isCopy,
            isMove = setting.edit.drag.isMove,
            prev = setting.edit.drag.prev,
            inner = setting.edit.drag.inner,
            next = setting.edit.drag.next;
            showCode(1, ['setting.edit.drag.isCopy = ' + isCopy, 'setting.edit.drag.isMove = ' + isMove]);
            showCode(2, ['setting.edit.drag.prev = ' + prev, 'setting.edit.drag.inner = ' + inner, 'setting.edit.drag.next = ' + next]);
        };

        function showCode(id, str) {
            var code = $("#code" + id);
            code.empty();
            for (var i=0, l=str.length; i<l; i++) {
                code.append("<li>"+str[i]+"</li>");
            }
        }

        $(document).ready(function(){
            $.fn.zTree.init($("#treeDemo"), setting, zNodes);
            setCheck();
            // $("#copy").bind("change", setCheck);
            $("#move").bind("change", setCheck);
            $("#prev").bind("change", setCheck);
            $("#inner").bind("change", setCheck);
            $("#next").bind("change", setCheck);
            $("#copy").bind("click", copy);
            $("#cut").bind("click", cut);
            $("#paste").bind("click", paste);
        });
        //-->
    </script>

</head>
<body>
    <div align="right"><a href="/logout">退出</a><br/></div>
    你好, ${name!}, 现在时间 ${date!}
    <div class="content_wrap">
        <div class="zTreeDemoBackground left">
            <ul id="treeDemo" class="ztree"></ul>
        </div>
        <div class="right">
            <ul class="info">
                <li class="title"><h2>1.this is a demo</h2>
                    <ul class="list">
                        <li>&nbsp;&nbsp;&nbsp;&nbsp;[ <a id="copy" href="#" title="复制" onclick="return false;">复制</a> ]
                            &nbsp;&nbsp;&nbsp;&nbsp;[ <a id="cut" href="#" title="剪切" onclick="return false;">剪切</a> ]
                            &nbsp;&nbsp;&nbsp;&nbsp;[ <a id="paste" href="#" title="粘贴" onclick="return false;">粘贴</a> ]</p></li>
                        <li class="highlight_red">just a demo</li>
                    </ul>
                </li>
            </ul>
        </div>
    </div>
</body>
</html>