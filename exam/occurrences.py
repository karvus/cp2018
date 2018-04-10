#!/usr/bin/python

import glob

files = 0
occurrences = {}
for filename in glob.iglob('/home/thomas/git/cp2018/exam/data_example/**/*.*', recursive=True):
    files = files + 1
    with open(filename) as file:
        for line in file:
            for num in [int(item) for item in line.split(",")]:
                if num == 2:
                    print(filename)
                if num in occurrences:
                    occurrences[num] = occurrences[num] + 1
                else:
                    occurrences[num] = 1
print(files)                    
print(occurrences)
                    
        
        
