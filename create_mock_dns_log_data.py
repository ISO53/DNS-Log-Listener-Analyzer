import time
import random

def generate_dns_log_entry():
    current_time = time.strftime("%m/%d/%Y %I:%M:%S %p")
    packet_id = f"{random.randint(0, 999999):08X}"
    ip_address = f"192.168.{random.randint(1, 255)}.{random.randint(1, 255)}"
    query_type = random.choice(["A", "AAAA", "MX", "PTR", "CNAME"])
    domain_name = ''.join(random.choice('abcdefghijklmnopqrstuvwxyz')
                          for _ in range(random.randint(5, 15)))

    dns_log_entry = f"{current_time} {packet_id} PACKET {ip_address} UDP Rcv {ip_address} 0002 Q [0001 D NOERROR] {query_type} ({len(domain_name)}){domain_name}(0)\n"
    return dns_log_entry

log_file_path = "foo.log"

while True:
    print("Generating mock data, to exit press CTRL + C")
    
    # Attempt to open the log file for writing, with a retry mechanism
    while True:
        try:
            with open(log_file_path, "a") as log_file:
                dns_entry = generate_dns_log_entry()
                log_file.write(dns_entry)
            break  # Exit the retry loop if the write was successful
        except PermissionError:
            print("Permission denied. Retrying in 1 second...")
            time.sleep(1)
    
    # You can uncomment the line below to add a random delay between log entries
    # time.sleep(random.uniform(1, 5))
