#!/bin/bash

# Define the output file for the changelog
changelog_file="changelog.md"

# Get content of the changelog file to append text at the top
changelog_content=$(cat "$changelog_file")

> "changelog_file"

current_branch=$(git rev-parse --abbrev-ref HEAD)

# Get the commit history using 'git log' command
git_log=$(git log "origin/$current_branch" "$current_branch" --pretty=format:"%h %aN %ae %ai")

# Initialize variables for changelog sections
added=""
changed=""
deprecated=""
removed=""
fixed=""
security=""

# Loop through each line of the log
while IFS= read -r line; do
    # Extract commit hash and comment
    commit_hash=$(echo "$line" | awk '{print $1}')
    comment=$(echo "$line" | cut -d " " -f2-)

    # Check if the comment starts with a specific keyword and add it to the corresponding section
    case $comment in
        (added*) added+="\n- ${comment#*(added)} ($commit_hash)" ;;
        (changed*) changed+="\n- ${comment#*(changed)} ($commit_hash)" ;;
        (deprecated*) deprecated+="\n- ${comment#*(deprecated)} ($commit_hash)" ;;
        (removed*) removed+="\n- ${comment#*(removed)} ($commit_hash)" ;;
        (fixed*) fixed+="\n- ${comment#*(fixed)} ($commit_hash)" ;;
        (security*) security+="\n- ${comment#*(security)} ($commit_hash)" ;;
    esac
done <<< "$changelog_file"

# Append the changelog content to the changelog file
echo -e "$changelog_content" >> "$changelog_file"

# Add the sections to the changelog file
echo -e "### Added\n$added" >> "$changelog_file"
echo -e "### Changed\n$changed" >> "$changelog_file"
echo -e "### Deprecated\n$deprecated" >> "$changelog_file"
echo -e "### Removed\n$removed" >> "$changelog_file"
echo -e "### Fixed\n$fixed" >> "$changelog_file"
echo -e "### Security\n$security" >> "$changelog_file"

echo "Changelog updated successfully."
