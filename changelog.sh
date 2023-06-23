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

# Add the sections to the changelog file
echo -e "## [$(date +"%Y-%m-%d %H:%M:%S")]\n" >> "$temp_file"
echo -e "### Added\n$added" >> "$temp_file"
echo -e "### Changed\n$changed" >> "$temp_file"
echo -e "### Deprecated\n$deprecated" >> "$temp_file"
echo -e "### Removed\n$removed" >> "$temp_file"
echo -e "### Fixed\n$fixed" >> "$temp_file"
echo -e "### Security\n$security" >> "$temp_file"
echo -e "### Uncategorized\n$uncategorized" >> "$temp_file"

# Append the existing contents of the changelog file to the temporary file
cat "$changelog_file" >> "$temp_file"

# Overwrite the changelog file with the contents of the temporary file
mv "$temp_file" "$changelog_file"

echo "Changelog updated successfully."
