import argparse
import sys

from shexter.config import configure, APP_NAME
from shexter.requester import DEFAULT_READ_COUNT, request


# Build help/usage, and the parser to determine options
def get_argparser():
    # description='Send and read texts using your ' + 'Android phone from the command line.'

    parser = argparse.ArgumentParser(prog='', usage='command [contact_name] [options]\n'
                                                    'You can also run "' + APP_NAME +
                                                    ' help to see commands and their options".')
    parser.add_argument('command', type=str,
                        help='Possible commands: Send $ContactName, Read $ContactName, Unread, Contacts, ' +
                             'SetPref $ContactName, Config. Not case sensitive.')
    parser.add_argument('contact_name', type=str, nargs='*',
                        help='Specify contact for SEND and READ commands.')
    parser.add_argument('-c', '--count', default=DEFAULT_READ_COUNT, type=int,
                        help='Specify how many messages to retrieve with the READ command. ' +
                             str(DEFAULT_READ_COUNT) + ' by default.')
    parser.add_argument('-m', '--multi', default=False, action='store_const', const=True,
                        help='Keep entering new messages to SEND until cancel signal is given. ' +
                             'Useful for sending multiple texts in succession.')
    parser.add_argument('-s', '--send', default=None, type=str,
                        help='Allows sending messages as a one-liner. Put your message after the flag. ' +
                             'Must be in quotes')
    parser.add_argument('-n', '--number', default=None, type=str,
                        help='Specify a phone number instead of a contact name for applicable commands.')

    return parser

CONFIG_COMMAND = 'config'
CONFIG_COMMAND_2 = 'configure'


# Main function to be called from -p mode. Pass the arguments directly to be parsed here.
def main(args_list):
    ip_addr = configure(False)
    parser = get_argparser()
    args = parser.parse_args(args_list)

    command = args.command.lower()
    if command == CONFIG_COMMAND or command == CONFIG_COMMAND_2:
        configure(True)
        quit()

    result = request(ip_addr, args)

    if result:
        print(result)
    else:
        parser.print_help()

    return result


# for calling shexter directly
if __name__ == "__main__":
    main(sys.argv[1:])
