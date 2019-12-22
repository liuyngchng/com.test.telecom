<!DOCTYPE html PUBLIC "-//W3C//DTD HTML 4.01 Transitional//EN" "http://www.w3.org/TR/html4/loose.dtd">
<html>
<head>
    <meta http-equiv="Content-Type" content="text/html; charset=UTF-8">
    <meta name="viewport" content="width=device-width, initial-scale=1, shrink-to-fit=no">
    <title>Index</title>
    <link rel="stylesheet" href="/css/demo.css">
    <link rel="stylesheet" href="/css/zTreeStyle/zTreeStyle.css">
    <!-- <script type="text/css" src="/css/bootstrap.min.css"></script>
    <script type="application/javascript" src="/js/jquery.ztree.all.js"></script> -->
    <script type="application/javascript" src="/js/jquery-1.4.4.min.js"></script>
    <script type="application/javascript" src="/js/jquery.ztree.core.js"></script>
    <script type="application/javascript" src="/js/jquery.ztree.excheck.js"></script>
    <script type="application/javascript" src="/js/jquery.ztree.exedit.js"></script>
    <script type="text/javascript">
        <!--
        var setting = {
            view: {
                showIcon: false,
                selectedMulti: false,
                dblClickExpand: false
            },
            data: {
                simpleData: {
                    enable: true
                }
            },
            edit: {
                enable: true,
                showRemoveBtn: false,
                showRenameBtn: true,
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
                beforeClick: beforeClick,
                onRightClick: OnRightClick
            }
        };

        var zNodes =[
            { id:1, pId:0, name:"节点1 ", open:true},
            { id:11, pId:1, name:"节点11"},
            { id:111, pId:11, name:"节点111"},
            { id:112, pId:11, name:"节点112"},
            { id:113, pId:11, name:"节点113"},
            { id:114, pId:11, name:"节点114"},
            { id:12, pId:1, name:"节点12"},
            { id:121, pId:12, name:"节点121"},
            { id:122, pId:12, name:"节点122"},
            { id:123, pId:12, name:"节点123"},
            { id:124, pId:12, name:"节点124"},
            { id:13, pId:1, name:"节点13", isParent:true},
            { id:2, pId:0, name:"节点2"},
            { id:21, pId:2, name:"节点21", open:true},
            { id:211, pId:21, name:"节点211"},
            { id:212, pId:21, name:"节点212"},
            { id:213, pId:21, name:"节点213"},
            { id:214, pId:21, name:"节点214"},
            { id:22, pId:2, name:"节点22"},
            { id:221, pId:22, name:"节点221"},
            { id:222, pId:22, name:"节点222"},
            { id:223, pId:22, name:"节点223"},
            { id:224, pId:22, name:"节点224"},
            { id:23, pId:2, name:"节点23"},
            { id:231, pId:23, name:"节点231"},
            { id:232, pId:23, name:"节点232"},
            { id:233, pId:23, name:"节点233"},
            { id:234, pId:23, name:"节点234"},
            { id:3, pId:0, name:"节点3", isParent:true}
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

        function showCode(str) {
            var code = $("#code");
            code.empty();
            for (var i=0, l=str.length; i<l; i++) {
                code.append("<li>"+str[i]+"</li>");
            }
        }

        function OnRightClick(event, treeId, treeNode) {
            if (!treeNode && event.target.tagName.toLowerCase() != "button" && $(event.target).parents("a").length == 0) {
                zTree.cancelSelectedNode();
                showRMenu("root", event.clientX, event.clientY);
            } else if (treeNode && !treeNode.noR) {
                zTree.selectNode(treeNode);
                showRMenu("node", event.clientX, event.clientY);
            }
        }

        function showRMenu(type, x, y) {
            $("#rMenu ul").show();
            if (type=="root") {
                $("#m_del").hide();
                $("#m_check").hide();
                $("#m_unCheck").hide();
            } else {
                $("#m_del").show();
                $("#m_check").show();
                $("#m_unCheck").show();
            }

            y += document.body.scrollTop;
            x += document.body.scrollLeft;
            rMenu.css({"top":y+"px", "left":x+"px", "visibility":"visible"});

            $("body").bind("mousedown", onBodyMouseDown);
        }
        function hideRMenu() {
            if (rMenu) rMenu.css({"visibility": "hidden"});
            $("body").unbind("mousedown", onBodyMouseDown);
        }
        function onBodyMouseDown(event){
            if (!(event.target.id == "rMenu" || $(event.target).parents("#rMenu").length>0)) {
                rMenu.css({"visibility" : "hidden"});
            }
        }
        var addCount = 1;
        function addTreeNode() {
            hideRMenu();
            var newNode = { name:"新增节点" + (addCount++)};
            if (zTree.getSelectedNodes()[0]) {
                newNode.checked = zTree.getSelectedNodes()[0].checked;
                zTree.addNodes(zTree.getSelectedNodes()[0], newNode);
            } else {
                zTree.addNodes(null, newNode);
            }
        }
        function removeTreeNode() {
            hideRMenu();
            var nodes = zTree.getSelectedNodes();
            if (nodes && nodes.length>0) {
                if (nodes[0].children && nodes[0].children.length > 0) {
                    var msg = "要删除的节点是父节点，如果删除将连同子节点一起删掉。\n\n请确认！";
                    if (confirm(msg)==true){
                        zTree.removeNode(nodes[0]);
                    }
                } else {
                    zTree.removeNode(nodes[0]);
                }
            }
        }
        function checkTreeNode(checked) {
            var nodes = zTree.getSelectedNodes();
            if (nodes && nodes.length>0) {
                zTree.checkNode(nodes[0], checked, true);
            }
            hideRMenu();
        }
        function resetTree() {
            hideRMenu();
            $.fn.zTree.init($("#treeDemo"), setting, zNodes);
        }

        function setEdit() {
            var zTree = $.fn.zTree.getZTreeObj("treeDemo");
            showCode(['setting.edit.showRenameBtn = true',
                'setting.edit.renameTitle = "重命名 "']);
        }

        var zTree, rMenu;
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
            $("#rename").bind("change", setEdit);
            zTree = $.fn.zTree.getZTreeObj("treeDemo");
            rMenu = $("#rMenu");
        });
        //-->
    </script>
    <script type="text/javascript">
        window.onload=function() {
            var ul=document.getElementById("treeDemo")
            document.oncontextmenu=function(ev) {
                var ev=ev||window.event
                var l=ev.clientX
                var t=ev.clientY
                ul.style.display="block"
                ul.style.left=l+'px'
                ul.style.top=t-16+'px'
                return false;
            }
        }
    </script>
    <style type="text/css">
        div#rMenu {position:absolute; visibility:hidden; top:0; background-color: #555;text-align: left;padding: 2px;}
        div#rMenu ul li{
            margin: 1px 0;
            padding: 0 5px;
            cursor: pointer;
            list-style: none outside none;
            background-color: #DFDFDF;
        }
    </style>

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
            <li class="title"><h2>上层应用</h2>
                <ul class="list">
                    <li>&nbsp;&nbsp;&nbsp;&nbsp;[ <a id="copy" href="#" title="复制" onclick="return false;">复制</a> ]
                        &nbsp;&nbsp;&nbsp;&nbsp;[ <a id="cut" href="#" title="剪切" onclick="return false;">剪切</a> ]
                        &nbsp;&nbsp;&nbsp;&nbsp;[ <a id="paste" href="#" title="粘贴" onclick="return false;">粘贴</a> ]</p></li>
                    <li class="highlight_red">上层应用</li>
                </ul>
            </li>
        </ul>
    </div>
</div>
<div id="rMenu">
    <ul>
        <li id="m_add" onclick="addTreeNode();">增加节点</li>
        <li id="m_del" onclick="removeTreeNode();">删除节点</li>
        <li id="m_reset" onclick="resetTree();">重置 </li>
    </ul>
</div>
</body>
</html>