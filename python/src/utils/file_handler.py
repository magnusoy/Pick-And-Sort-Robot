# #!/usr/bin/env python3
# -*- coding: utf-8 -*-

import os

class FileController:
    """docstring"""

    def __init__(self, file_path):
        self.file_path = file_path

    
    def write(self, msg):
        """docstring"""
        success = False
        try:
            with open(self.file_path, 'w') as f:
                f.write(msg)
            success = True
        except FileNotFoundError:
            print("File does not exits.")
        finally:
            return success
        
    
    def read(self):
        """docstring"""
        content = ''
        try:
            with open(self.file_path, 'r') as f:
                content = f.read()
        except FileNotFoundError:
            print("File does not exits.")
        finally:
            return content


    def append(self, msg):
        """docstring"""
        success = False
        try:
            with open(self.file_path, 'rb') as f:
                f.write(msg)
            success = True
        except FileNotFoundError:
            print("File does not exits.")
        finally:
            return success


# Example of usage
if __name__ == "__main__":
    jsonc = FileController(".../../../../../resources/Objects/temp.json")
    print(jsonc.read())
    