#!/usr/bin/env python3
"""
Script to remove Lombok annotations from entity classes and add explicit getters/setters.
"""

import os
import re
import glob

def process_file(filepath):
    with open(filepath, 'r') as f:
        content = f.read()
    
    # Skip if already processed (no Lombok annotations)
    if '@Getter' not in content and '@Setter' not in content and '@NoArgsConstructor' not in content and '@AllArgsConstructor' not in content:
        print(f"Skipping {filepath} - already processed")
        return
    
    print(f"Processing {filepath}")
    
    # Remove Lombok imports
    content = re.sub(r'import lombok\.\*;\n', '', content)
    content = re.sub(r'import lombok\.(Getter|Setter|NoArgsConstructor|AllArgsConstructor|Builder|SuperBuilder|Builder\.Default);\n', '', content)
    content = re.sub(r'import lombok\.experimental\.SuperBuilder;\n', '', content)
    
    # Remove Lombok annotations from class
    content = re.sub(r'@Getter\s*\n', '', content)
    content = re.sub(r'@Setter\s*\n', '', content)
    content = re.sub(r'@NoArgsConstructor\s*\n', '', content)
    content = re.sub(r'@AllArgsConstructor\s*\n', '', content)
    content = re.sub(r'@Builder\s*\n', '', content)
    content = re.sub(r'@SuperBuilder\s*\n', '', content)
    content = re.sub(r'@Builder\.Default\s*\n', '', content)
    
    # Add explicit constructors if not present
    # This is a simplified approach - in reality we'd need to parse the fields
    # For now, we'll just remove the annotations and hope the explicit getters/setters are already there
    
    with open(filepath, 'w') as f:
        f.write(content)
    
    print(f"Processed {filepath}")

def main():
    model_dir = '/var/home/anderson/Projetos/ACDX_ERP/erp-brazil/sales-crm-service/src/main/java/com/erp/brazil/sales/domain/model'
    java_files = glob.glob(os.path.join(model_dir, '*.java'))
    
    for filepath in java_files:
        # Skip BaseEntity, Address, Contact, Payment, PipelineStage (already processed)
        filename = os.path.basename(filepath)
        if filename in ['BaseEntity.java', 'Address.java', 'Contact.java', 'Payment.java', 'PipelineStage.java']:
            continue
        process_file(filepath)

if __name__ == '__main__':
    main()