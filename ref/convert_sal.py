#!/usr/bin/python3
# -*- coding: utf-8 -*-

from saleae import automation

file_path = '/home/lars/prj/logic_reporter/maven/ref/saleae.sal'
export_path = '/home/lars/prj/logic_reporter/maven/ref'

# Connect to the running Logic 2 Application on port `10430`
manager = automation.Manager(port=10430)
with manager.load_capture(file_path) as capture:
    capture.export_raw_data_binary(export_path)

manager.close()
