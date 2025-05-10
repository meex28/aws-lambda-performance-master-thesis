import {CloudWatchLogs, S3} from 'aws-sdk';
import {convertTimestampToDate, getFunctionNames} from "../common";

const cloudWatchLogs = new CloudWatchLogs();
const s3 = new S3();

const RESULTS_BUCKET = "mte-benchmark-results"

interface LambdaInvokeLogReport {
    requestId: string;
    duration: number;
    billedDuration: number;
    memorySize: number;
    maxMemoryUsed: number;
    initDuration?: number;
    restoreDuration?: number;
    billedRestoreDuration?: number;
    timestamp: number;
}

export const handler = async (): Promise<any> => {
    try {
        const startTime = Date.now()
        const functionNames = await getFunctionNames()

        const results: Record<string, LambdaInvokeLogReport[]> = {};

        for (const functionName of functionNames) {
            console.log(`Analyzing logs for function: ${functionName}`);

            results[functionName] = await getReportLogsForFunction(functionName);
        }

        const uploadResults = await uploadObjectToS3(
            RESULTS_BUCKET,
            `results-${convertTimestampToDate(startTime).toISOString()}.json`,
            results
        )

        return {
            statusCode: 200,
            body: JSON.stringify({
                message: 'Log analysis completed',
                startTimeAnalyzed: new Date(startTime * 1000).toISOString(),
                resultsPath: uploadResults.Location
            })
        };
    } catch (error) {
        console.error('Error in lambda handler:', error);
        return {
            statusCode: 500,
            body: JSON.stringify({
                message: 'Error analyzing logs',
                error: error instanceof Error ? error.message : String(error)
            })
        };
    }
};

async function getReportLogsForFunction(functionName: string): Promise<LambdaInvokeLogReport[]> {
    const logGroupName = `/aws/lambda/${functionName}`;
    const logStreams = await getLogStreams(logGroupName);
    const reports: LambdaInvokeLogReport[] = [];

    for (const stream of logStreams) {
        const streamName = stream.logStreamName;
        if (!streamName) continue;

        const logs = await getLogsFromStream(logGroupName, streamName);

        for (const log of logs) {
            if (log.message && log.message.startsWith('REPORT')) {
                const reportData = parseReportLog(log.message, log.timestamp || 0);
                if (reportData) {
                    reports.push(reportData);
                }
            }
        }
    }

    return reports;
}

async function getLogStreams(logGroupName: string): Promise<CloudWatchLogs.LogStream[]> {
    try {
        const streams: CloudWatchLogs.LogStream[] = [];
        let nextToken: string | undefined;

        do {
            const response = await cloudWatchLogs.describeLogStreams({
                logGroupName,
                orderBy: 'LastEventTime',
                descending: true,
                nextToken
            }).promise();

            const relevantStreams = response.logStreams || []

            streams.push(...relevantStreams);
            nextToken = response.nextToken;
        } while (nextToken);

        return streams;
    } catch (error) {
        console.error(`Error getting log streams for ${logGroupName}:`, error);
        return [];
    }
}

async function getLogsFromStream(
    logGroupName: string,
    logStreamName: string,
): Promise<CloudWatchLogs.OutputLogEvent[]> {
    try {
        const logs: CloudWatchLogs.OutputLogEvent[] = [];
        let nextToken: string | undefined;

        do {
            const response = await cloudWatchLogs.getLogEvents({
                logGroupName,
                logStreamName,
                startFromHead: true,
                nextToken
            }).promise();

            if (response.events) {
                logs.push(...response.events);
            }

            nextToken = response.nextForwardToken;

            if (logs.length > 0 && nextToken && nextToken === response.nextForwardToken) {
                break;
            }
        } while (nextToken);

        return logs;
    } catch (error) {
        console.error(`Error getting logs from stream ${logStreamName}:`, error);
        return [];
    }
}

function parseReportLog(logMessage: string, timestamp: number): LambdaInvokeLogReport | null {
    try {
        const requestIdMatch = logMessage.match(/RequestId: ([a-f0-9-]+)/);
        const durationMatch = logMessage.match(/Duration: ([0-9.]+) ms/);
        const billedDurationMatch = logMessage.match(/Billed Duration: ([0-9.]+) ms/);
        const memorySizeMatch = logMessage.match(/Memory Size: ([0-9]+) MB/);
        const maxMemoryUsedMatch = logMessage.match(/Max Memory Used: ([0-9]+) MB/);
        const initDurationMatch = logMessage.match(/Init Duration: ([0-9.]+) ms/);
        const restoreDurationMatch = logMessage.match(/Restore Duration: ([0-9.]+) ms/);
        const billedRestoreDurationMatch = logMessage.match(/Billed Restore Duration: ([0-9.]+) ms/);

        if (!requestIdMatch || !durationMatch || !billedDurationMatch || !memorySizeMatch || !maxMemoryUsedMatch) {
            return null;
        }

        return {
            requestId: requestIdMatch[1],
            duration: parseFloat(durationMatch[1]),
            billedDuration: parseFloat(billedDurationMatch[1]),
            memorySize: parseInt(memorySizeMatch[1], 10),
            maxMemoryUsed: parseInt(maxMemoryUsedMatch[1], 10),
            initDuration: initDurationMatch ? parseFloat(initDurationMatch[1]) : undefined,
            restoreDuration: restoreDurationMatch ? parseFloat(restoreDurationMatch[1]) : undefined,
            billedRestoreDuration: billedRestoreDurationMatch ? parseFloat(billedRestoreDurationMatch[1]) : undefined,
            timestamp
        };
    } catch (error) {
        console.error('Error parsing REPORT log:', error);
        return null;
    }
}

const uploadObjectToS3 = async (
    bucket: string,
    path: string,
    data: object
): Promise<S3.ManagedUpload.SendData> => {
    try {
        const params: S3.PutObjectRequest = {
            Bucket: bucket,
            Key: path,
            Body: JSON.stringify(data),
            ContentType: 'application/json',
        };

        return await s3.upload(params).promise();
    } catch (error) {
        console.error('Error uploading to S3:', error);
        throw error;
    }
}
