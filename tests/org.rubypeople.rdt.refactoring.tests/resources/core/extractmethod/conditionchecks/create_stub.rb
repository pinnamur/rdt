#!/usr/bin/ruby

name = "extract_method_checker_test_"
max = 0
Dir.new(".").each do |file| 
  if file =~ /extract_method_checker_test_(\d+)/
    max = $1.to_i if $1.to_i > max
  end
end

name = name + (max + 1).to_s
puts name

`touch #{name}.test_source`

property = <<EOF
newMethodName=
start=
end=
initialError0=
initialError1=
#....
initialWarning0=
initialWarning1=
#...
finalError0=
finalError1=
#...
finalWarning0=
finalWarning1=
#...
EOF

`echo "#{property}" > #{name}.test_properties`


