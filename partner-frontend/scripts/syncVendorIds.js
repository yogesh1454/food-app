/**
 * Script to update vendorId in Firestore using REST API with Firebase CLI token
 */

const https = require('https');
const fs = require('fs');
const path = require('path');

const PROJECT_ID = 'food-app-1feee';
const COLLECTION_ID = 'userMappings';

// Mapping of Firebase UID to vendorId
const VENDOR_MAPPINGS = {
    'x0IudnlZY5cYTqUXEDqtJQobd3N2': 6,  // hcchc@gmail.com -> Vendor 6 (Tddtdy)
};

function getAccessToken() {
    const configPath = path.join(process.env.HOME, '.config/configstore/firebase-tools.json');
    try {
        const config = JSON.parse(fs.readFileSync(configPath, 'utf8'));
        return config.tokens?.access_token;
    } catch (e) {
        console.error('Could not read Firebase config:', e.message);
        return null;
    }
}

function updateDocument(firebaseUid, vendorId, accessToken) {
    return new Promise((resolve, reject) => {
        const documentPath = `projects/${PROJECT_ID}/databases/(default)/documents/${COLLECTION_ID}/${firebaseUid}`;

        const data = JSON.stringify({
            fields: {
                vendorId: { integerValue: vendorId.toString() },
                vendorIdSavedAt: { stringValue: new Date().toISOString() }
            }
        });

        const options = {
            hostname: 'firestore.googleapis.com',
            port: 443,
            path: `/v1/${documentPath}?updateMask.fieldPaths=vendorId&updateMask.fieldPaths=vendorIdSavedAt`,
            method: 'PATCH',
            headers: {
                'Authorization': `Bearer ${accessToken}`,
                'Content-Type': 'application/json',
                'Content-Length': Buffer.byteLength(data)
            }
        };

        const req = https.request(options, (res) => {
            let body = '';
            res.on('data', chunk => body += chunk);
            res.on('end', () => {
                if (res.statusCode === 200) {
                    resolve({ success: true, data: JSON.parse(body) });
                } else {
                    resolve({ success: false, status: res.statusCode, body });
                }
            });
        });

        req.on('error', reject);
        req.write(data);
        req.end();
    });
}

async function main() {
    console.log('VendorId Sync Script\n');

    const accessToken = getAccessToken();
    if (!accessToken) {
        console.error('No access token found. Please run "firebase login" first.');
        process.exit(1);
    }

    console.log('Access token found, updating Firestore documents...\n');

    for (const [firebaseUid, vendorId] of Object.entries(VENDOR_MAPPINGS)) {
        console.log(`Updating ${firebaseUid} with vendorId: ${vendorId}`);

        try {
            const result = await updateDocument(firebaseUid, vendorId, accessToken);
            if (result.success) {
                console.log(`✅ Successfully updated ${firebaseUid}`);
            } else {
                console.log(`❌ Failed to update ${firebaseUid}: ${result.status}`);
                console.log(`   Response: ${result.body}`);
            }
        } catch (error) {
            console.log(`❌ Error updating ${firebaseUid}: ${error.message}`);
        }
        console.log('');
    }

    console.log('Done!');
}

main();
