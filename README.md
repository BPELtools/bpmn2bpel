# BPMN2BPEL

This project provides a BPMN2BPEL converter using Eclipse technology

## Setup

1. `cd org.opentosca.bpmn2bpel.converter`
2. run `gradlew jar`. Ignore any error. Now all dependencies should be fetched
3. Import all projects into your eclipse workspace.
4. Create Eclipse directory variable `gradle_home` pointing to `c:\users\<loginname>\.gradle`.
5. Go to project `org.opentosca.bpmn2bpel.converter.test` and open `bpmn2bpel.test.target`.
6. Click "Set as target platform"
7. Run all tests

## Further reading

- Diploma Thesis [Eine BPMN-nach-BPEL-Transformation](http://www2.informatik.uni-stuttgart.de/cgi-bin/NCSTRL/NCSTRL_view.pl?id=DIP-3324&mod=0&engl=0&inst=IAAS)

## License

This work is licenced under the [Apache License v2.0].

 [Apache License v2.0]: http://www.apache.org/licenses/LICENSE-2.0.html
