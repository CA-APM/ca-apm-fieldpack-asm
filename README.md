# CA APM App Synthetic Monitor Integration

## Description
This extension integrates metrics from CA App Synthetic Monitor (fka Cloud Monitor, WatchMouse) into CA APM. It provides a lot more configuration flexibility and more information than the out-of-the-box extension that is provided in the CA APM product.

### Short Description
This extension integrates metrics from CA App Synthetic Monitor (fka Cloud Monitor, WatchMouse) into CA APM.

## APM version
This extension has been tested with CA APM 9.7 and 10.0.

## Supported third party versions
This extension has been tested with CA App Synthetic Monitor 8.5.

## License
This extension is provided under the [Eclipse Public License - v 1.0](https://github.com/CA-APM/ca-apm-fieldpack-asm/blob/master/LICENSE) license.


# Installation and Usage Instructions
See [CA APM App Synthetic Monitor Agent.docx](https://github.com/CA-APM/ca-apm-fieldpack-asm/blob/master/asm-monitor/src/main/docs/CA%20APM%20App%20Synthetic%20Monitor%20Agent.docx).

## Dependencies
CA APM EPAgent 9.7, active CA App Synthetic Monitor account with API access.

## Support
This document and associated tools are made available from CA Technologies as examples and provided at no charge as a courtesy to the CA APM Community at large. This resource may require modification for use in your environment. However, please note that this resource is not supported by CA Technologies, and inclusion in this site should not be construed to be an endorsement or recommendation by CA Technologies. These utilities are not covered by the CA Technologies software license agreement and there is no explicit or implied warranty from CA Technologies. They can be used and distributed freely amongst the CA APM Community, but not sold. As such, they are unsupported software, provided as is without warranty of any kind, express or implied, including but not limited to warranties of merchantability and fitness for a particular purpose. CA Technologies does not warrant that this resource will meet your requirements or that the operation of the resource will be uninterrupted or error free or that any defects will be corrected. The use of this resource implies that you understand and agree to the terms listed herein.

Although these utilities are unsupported, please let us know if you have any problems or questions by adding a comment to the CA APM Community Site area where the resource is located, so that the Author(s) may attempt to address the issue or question.

Unless explicitly stated otherwise this extension is only supported on the same platforms as the APM core agent. See [APM Compatibility Guide](http://www.ca.com/us/support/ca-support-online/product-content/status/compatibility-matrix/application-performance-management-compatibility-guide.aspx).

### Support URL
https://github.com/CA-APM/ca-apm-fieldpack-asm/issues

## Categories
Integration

# Change log
Changes for each version of the extension.

Version | Author | Comment
--------|--------|--------
1.0 | Seth Schwartzman | First version of the extension.
1.1 | Guenter Grossberger | Overhaul of the whole extension adding more configuration points and information about individual script step results.
1.2 | Alex Bradley, Martin Macura, Guenter Grossberger | Download new HAR file format, add JMeter label to metric path (optional), by default don't download 'full' logs, avoid high cpu in unzip
1.2.1 | Guenter Grossberger | added asm.skipNoCheckpointAvailable property and changed artifact and file names
