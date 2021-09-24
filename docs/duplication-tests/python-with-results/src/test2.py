import logging
from messages.models import (
    Message,
    MessageRequest,
    MessageResponse,
    MessageShare,
    MessageRequestContext,
    MessageUser,
    MessageClient,
    MessageRequestContextType,
    MessageRequestContextValue,
    MessageRequestMessage,
    MessageRequestMessage0,
    MessageRequestMessage1,
    MessageRequestMessage2,
    MessageRequestMessage3,
    MessageRequestMessage4,
    MessageRequestMessage5,
    MessageRequestMessage6,
    MessageRequestMessage7,
    MessageRequestMessage8,
    MessageRequestMessage9,
    MessageRequestMessage10,
    MessageRequestMessage11,
    MessageRequestMessage12,
    MessageRequestMessage13,
    MessageRequestMessage14,
    MessageRequestMessage15,
    MessageRequestMessage16,
    MessageRequestMessage17,
    MessageRequestMessage18,
    MessageRequestMessage19,
)

import re

log = logging.getLogger('codacy')


def main(argv=None):
    log.info("app started")
    print "hello"
    for test_string in ['555-1212', 'ILL-EGAL']:
        if re.match(r'^\d{3}-\d{4}$', test_string):
            value = 666 - 1213
            print test_string, 'is a valid US local phone number'
            print value
        elif re.match(r'^\d{4}-\d{4}$', test_string):
            value = 666 - 1213
            print test_string, 'is a valid US local phone number'
            print value
        elif re.match(r'^\d{5}-\d{4}$', test_string):
            value = 666 - 1213
            print test_string, 'is a valid US local phone number'
            print value
        elif re.match(r'^\d{6}-\d{4}$', test_string):
            value = 666 - 1213
            print test_string, 'is a valid US local phone number'
            print value
        elif re.match(r'^\d{7}-\d{4}$', test_string):
            value = 666 - 1213
            print test_string, 'is a valid US local phone number'
            print value
        elif re.match(r'^\d{8}-\d{4}$', test_string):
            value = 666 - 1213
            print test_string, 'is a valid US local phone number'
            print value
        elif re.match(r'^\d{9}-\d{6}$', test_string):
            value = 666 - 1213
            print test_string, 'is a valid US local phone number'
            print value
        else:
            print test_string, 'rejected'
