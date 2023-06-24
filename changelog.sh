#!/bin/bash

echo -e "Processing changelog..."


# Define the output file for the changelog
changelog_path="../../../"
changelog_file="changelog.md"

# Get current tag and latest remote tag
current_tag=$(git describe --tags --abbrev=0)
latest_remote_tag=$(git -c 'versionsort.suffix=-' ls-remote --tags --sort='v:refname' "$(git config --get remote.origin.url)" | tail -1 | awk -F/ '{print $3}')

# if current_tag and latest_remote_tag are equals, exit with 1
if [[ "$current_tag" == "$latest_remote_tag" ]]; then
   echo "Latest tag ($current_tag) is already pushed to remote. Maybe you forgot to increment the version?"
   read -p '(y/n): ' answer
   if [[ "$answer" == "y" ]]; then
	exit 1	
   fi
fi

# Get current branch name
current_branch=$(git rev-parse --abbrev-ref HEAD)

# Get the commit history using 'git log' command
git_log=$(git log "origin/$current_branch..$current_branch" --pretty=format:"%h %s")

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
        echo -e "### $section_name\n$section_content\n" >> "$temp_file"
    fi
}

# Add date time and git tag (if present) to the temporary file
echo -e "# [$current_tag] - $(date +"%Y-%m-%d %H:%M:%S")\n" >> "$temp_file"

# Append sections to the temporary file
append_section "Added" "$added"
append_section "Changed" "$changed"
append_section "Deprecated" "$deprecated"
append_section "Removed" "$removed"
append_section "Fixed" "$fixed"
append_section "Security" "$security"
append_section "Uncategorized" "$uncategorized"

# Append the existing contents of the changelog file to the temporary file
cat "$changelog_path$changelog_file" >> "$temp_file"

# Overwrite the changelog file with the contents of the temporary file
mv "$temp_file" "$changelog_path$changelog_file"

echo "Changelog updated successfully!"

echo -e "Committing..."

cd $changelog_path
git add $changelog_file
git commit -m "(changed) updated changelog.md"

exit 0