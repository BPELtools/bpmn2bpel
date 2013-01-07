#!/usr/bin/env python3
import logging
import os
import sys

logging.basicConfig(level=logging.DEBUG)
#logging.basicConfig(level=logging.INFO)

bpmnDir = 'src/test/resources/bpmn/'

#from http://stackoverflow.com/a/6007172/873282
#required if filenames contain en dash (u2013)
fs_enc = sys.getfilesystemencoding()
for dirpath, dirnames, filenames in os.walk(bpmnDir.encode(fs_enc)):
    for filename in filenames:
        relDir = os.path.join(dirpath[len(bpmnDir):].decode(fs_enc))
        if filename.endswith(b".xml") and (not filename.endswith(b".out.xml")):
            print()
            print('    @Test')
            relDir = relDir.replace("\\", "/")
            methodName = relDir.replace("/", "_")
            methodName = methodName.replace("-", "_")
            methodName = methodName + "_"
            filename = str(filename.decode(fs_enc))[0:-4]
            methodName = methodName + filename.replace("-", "_")
            methodName = methodName.replace(" ", "_")
            sys.stdout.write('    public void test')
            sys.stdout.write(methodName)
            print('() throws IOException, SAXException {')
            sys.stdout.write('        this.testProcess("')
            sys.stdout.write(relDir)
            sys.stdout.write('", "')
            sys.stdout.write(filename)
            print('");')
            print('    }')
