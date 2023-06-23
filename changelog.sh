#!/bin/bash

# Define the output file for the changelog
changelog_file="changelog.md"

current_branch=$(git rev-parse --abbrev-ref HEAD)

# Get the commit history using 'git log' command
git_log=$(git log "origin/$current_branch" "$current_branch" --pretty=format:"%h %s")

# Initialize variables for changelog sections
added=""
changed=""
deprecated=""
removed=""
fixed=""
security=""
uncategorized=""

# Loop through each line of the log
while IFS= read -r line; do
    # Extract commit hash and comment
    commit_hash=$(echo "$line" | awk '{print $1}')
    comment=$(echo "$line" | cut -d " " -f2-)

    echo "$comment"

    # Check if the comment starts with a specific keyword and add it to the corresponding section
    case $comment in
        "(added)"* | *add*) added+="\n- ${comment#*(added)} ($commit_hash)" ;;
        "(changed)"* | *change* | *update*) changed+="\n- ${comment#*(changed)} ($commit_hash)" ;;
        "(deprecated)"* | *deprecate*) deprecated+="\n- ${comment#*(deprecated)} ($commit_hash)" ;;
        "(removed)"* | *remove*) removed+="\n- ${comment#*(removed)} ($commit_hash)" ;;
        "(fixed)"* | *fix*) fixed+="\n- ${comment#*(fixed)} ($commit_hash)" ;;
        "(security)"* | *secure*) security+="\n- ${comment#*(security)} ($commit_hash)" ;;
        *) uncategorized+="\n- $comment ($commit_hash)" ;;
    esac
done <<< "$git_log"

# Create a temporary file
temp_file=$(mktemp)

# Function to append section to the temporary file
append_section() {
    local section_name="$1"
    local section_content="$2"

    if [[ -n "$section_content" ]]; then
        echo -e "### $section_name\n$section_content" >> "$temp_file"
    fi
}

# Add the sections to the changelog file
echo -e "# [$(date +"%Y-%m-%d %H:%M:%S")]\n" >> "$temp_file"
# Append sections to the temporary file
append_section "Added" "$added"
append_section "Changed" "$changed"
append_section "Deprecated" "$deprecated"
append_section "Removed" "$removed"
append_section "Fixed" "$fixed"
append_section "Security" "$security"
append_section "Uncategorized" "$uncategorized"

# Append the existing contents of the changelog file to the temporary file
cat "$changelog_file" >> "$temp_file"

# Overwrite the changelog file with the contents of the temporary file
mv "$temp_file" "$changelog_file"

echo "Changelog updated successfully."
