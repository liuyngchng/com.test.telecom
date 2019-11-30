<!DOCTYPE html PUBLIC "-//W3C//DTD HTML 4.01 Transitional//EN" "http://www.w3.org/TR/html4/loose.dtd">
<html>
    <head>
        <meta http-equiv="Content-type" content="text/html; charset=UTF-8">
        <meta name="viewport" content="width=device-width, initial-scale=1, shrink-to-fit=no">
        <title>单文件上传</title>
    </head>
    <body>
        <form method="post" action="/upload" enctype="multipart/form-data">
            <input type="file" name="file"><br>
            <input type="submit" value="提交">
        </form>
    </body>
</html>