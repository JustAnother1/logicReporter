#!/usr/bin/python3
# -*- coding: utf-8 -*-

import sys
import os
from saleae import automation

# for this to work the first parameter must be the name of a *.sal file
# and the Logic application must be running.


def convert(file_path, export_path):
    # Connect to the running Logic 2 Application on port `10430`
    manager = automation.Manager(port=10430)
    with manager.load_capture(file_path) as capture:
        capture.export_raw_data_binary(export_path)
    manager.close()



if __name__ == '__main__':
    if 2 > len(sys.argv):
        print('provide file name as parameter!')
        sys.exit(1)

    cwd = os.getcwd()
    file_path = cwd + '/' + sys.argv[1]
    export_path = cwd + '/' + sys.argv[1][:-4]
    print('file_path: ' + file_path)
    print('export_path: ' + export_path)
    os.makedirs(export_path)

    convert(file_path, export_path)
